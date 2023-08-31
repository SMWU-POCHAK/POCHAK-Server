package com.apps.pochak.tag.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.apps.pochak.common.BaseException;
import com.apps.pochak.tag.domain.Tag;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class TagRepository {
    private final TagCrudRepository tagCrudRepository;
    private final DynamoDBMapper mapper;

    public Tag save(Tag tag) {
        return tagCrudRepository.save(tag);
    }

    public TagData findPublicTagsByUserHandle(String userHandle,
                                              Map<String, AttributeValue> exclusiveStartKey) throws BaseException {

        HashMap<String, String> ean = new HashMap<>();
        ean.put("#PK", "PartitionKey");
        ean.put("#SK", "SortKey");

        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":val1", new AttributeValue().withS(userHandle));
        eav.put(":val2", new AttributeValue().withS("TAG#"));

        DynamoDBQueryExpression<Tag> query = new DynamoDBQueryExpression<Tag>()
                .withKeyConditionExpression("#PK = :val1 and begins_with(#SK, :val2)")
                .withExpressionAttributeValues(eav)
                .withExpressionAttributeNames(ean)
                .withExclusiveStartKey(exclusiveStartKey) // 없으면 null로
                .withLimit(12) // TODO: 테스트 후 한번에 못 가져오면 개수 조정 필요
                .withScanIndexForward(false); // desc

        QueryResultPage<Tag> tagQueryResultPage = mapper.queryPage(Tag.class, query);

        return new TagData(tagQueryResultPage.getResults(), tagQueryResultPage.getLastEvaluatedKey());
    }

    @Data
    @NoArgsConstructor
    public class TagData {
        private List<Tag> result;
        private Map<String, AttributeValue> exclusiveStartKey;

        public TagData(List<Tag> result, Map<String, AttributeValue> exclusiveStartKey) {
            this.result = result;
            this.exclusiveStartKey = exclusiveStartKey;
        }
    }
}
