package com.apps.pochak.tag.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.apps.pochak.common.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@NoArgsConstructor
@DynamoDBTable(tableName = "pochakdatabase")
public class Tag extends BaseEntity {
    @Id
    private TagId tagId;

    private String userHandle;

    // 앞에 Tag#를 붙일 예정
    private String allowedDate;

    @DynamoDBAttribute
    @Getter
    @Setter
    private String postPK;

    @DynamoDBAttribute
    @Getter
    @Setter
    private String postImg;

    @DynamoDBHashKey(attributeName = "PartitionKey")
    public String getUserHandle() {
        return tagId != null ? tagId.getUserHandle() : null;
    }

    public void setUserHandle(String userHandle) {
        if (tagId == null) {
            tagId = new TagId();
        }
        tagId.setUserHandle(userHandle);
    }

    @DynamoDBRangeKey(attributeName = "SortKey")
    public String getAllowedDate() {
        return tagId != null ? tagId.getAllowedDate() : null;
    }

    public void setAllowedDate(LocalDateTime allowedDate) {
        if (tagId == null) {
            tagId = new TagId();
        }
        tagId.setAllowedDate("TAG#" + allowedDate.toString());
    }
}
