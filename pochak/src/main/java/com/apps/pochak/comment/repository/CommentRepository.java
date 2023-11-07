package com.apps.pochak.comment.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.apps.pochak.comment.domain.Comment;
import com.apps.pochak.comment.domain.CommentId;
import com.apps.pochak.common.BaseException;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.apps.pochak.common.BaseResponseStatus.INVALID_COMMENT_ID;
import static com.apps.pochak.common.BaseResponseStatus.INVALID_COMMENT_SK;
import static com.apps.pochak.common.Status.DELETED;
import static com.apps.pochak.common.Status.PUBLIC;

@Repository
@RequiredArgsConstructor
public class CommentRepository {

    private final CommentCrudRepository commentCrudRepository;
    private final DynamoDBMapper mapper;

    public Comment findCommentByCommentId(CommentId commentId) throws BaseException {
        // commentId에 해당하는 Comment 찾기
        return commentCrudRepository.findById(commentId).orElseThrow(() -> new BaseException(INVALID_COMMENT_ID));
    }

    public void deleteComment(Comment deleteComment) throws BaseException {
        commentCrudRepository.delete(deleteComment);
    }


    public Comment saveComment(Comment comment) {
        return commentCrudRepository.save(comment);
    }

    public Comment findRandomCommentsByPostPK(String postPK) throws BaseException {
        HashMap<String, String> ean = new HashMap<>();
        ean.put("#PK", "PartitionKey");
        ean.put("#SK", "SortKey");
        ean.put("#STATUS", "status");

        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":val1", new AttributeValue().withS(postPK));
        eav.put(":val2", new AttributeValue().withS("COMMENT#"));
        eav.put(":val3", new AttributeValue().withS(PUBLIC.toString()));

        DynamoDBQueryExpression<Comment> query = new DynamoDBQueryExpression<Comment>()
                .withKeyConditionExpression("#PK = :val1 and begins_with(#SK, :val2)")
                .withFilterExpression("#STATUS = :val3")
                .withExpressionAttributeValues(eav)
                .withExpressionAttributeNames(ean);

        List<Comment> comments = mapper.query(Comment.class, query);

        if (comments.isEmpty()) {
            return null;
        }
        return comments.get((int) (Math.random() * comments.size()));
    }

    public CommentData findCommentsByPostPK(String postPK,
                                            Map<String, AttributeValue> exclusiveStartKey) {
        HashMap<String, String> ean = new HashMap<>();
        ean.put("#PK", "PartitionKey");
        ean.put("#SK", "SortKey");
        ean.put("#STATUS", "status");

        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":val1", new AttributeValue().withS(postPK));
        eav.put(":val2", new AttributeValue().withS("COMMENT#"));
        eav.put(":val3", new AttributeValue().withS(PUBLIC.toString()));

        DynamoDBQueryExpression<Comment> query = new DynamoDBQueryExpression<Comment>()
                .withKeyConditionExpression("#PK = :val1 and begins_with(#SK, :val2)")
                .withFilterExpression("#STATUS = :val3")
                .withExpressionAttributeValues(eav)
                .withExpressionAttributeNames(ean)
                .withExclusiveStartKey(exclusiveStartKey)
                .withLimit(12)
                .withScanIndexForward(false);

        QueryResultPage<Comment> commentQueryResultPage = mapper.queryPage(Comment.class, query);
        Map<String, String> resultLastEvaluatedKey = (commentQueryResultPage.getLastEvaluatedKey() == null) ?
                null
                : commentQueryResultPage.getLastEvaluatedKey().entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().getS()
                        )
                );

        return new CommentData(commentQueryResultPage.getResults(), resultLastEvaluatedKey);
    }

    public List<Comment> findParentCommentsByPostPK(String postPK) {
        HashMap<String, String> ean = new HashMap<>();
        ean.put("#PK", "PartitionKey");
        ean.put("#SK", "SortKey");
        ean.put("#STATUS", "status");

        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":val1", new AttributeValue().withS(postPK));
        eav.put(":val2", new AttributeValue().withS("COMMENT#"));
        eav.put(":val3", new AttributeValue().withS(PUBLIC.toString()));

        DynamoDBQueryExpression<Comment> query = new DynamoDBQueryExpression<Comment>()
                .withKeyConditionExpression("#PK = :val1 and begins_with(#SK, :val2)")
                .withFilterExpression("#STATUS = :val3")
                .withExpressionAttributeValues(eav)
                .withExpressionAttributeNames(ean);

        QueryResultPage<Comment> commentQueryResultPage = mapper.queryPage(Comment.class, query);
        return commentQueryResultPage.getResults();
    }

    public List<Comment> findChildCommentByParentCommentSKAndPostPK(String parentCommentSK, String postPK) {
        HashMap<String, String> ean = new HashMap<>();
        ean.put("#PK", "PartitionKey");
        ean.put("#SK", "SortKey");
        ean.put("#STATUS", "status");
        ean.put("#PARENT_SK", "parentCommentSK");

        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":val1", new AttributeValue().withS(postPK));
        eav.put(":val2", new AttributeValue().withS("COMMENT#CHILD#"));
        eav.put(":val3", new AttributeValue().withS(PUBLIC.toString()));
        eav.put(":val4", new AttributeValue().withS(parentCommentSK));

        DynamoDBQueryExpression<Comment> query = new DynamoDBQueryExpression<Comment>()
                .withKeyConditionExpression("#PK = :val1 and begins_with(#SK, :val2)")
                .withFilterExpression("#STATUS = :val3 and #PARENT_SK = :val4")
                .withExpressionAttributeValues(eav)
                .withExpressionAttributeNames(ean);

        QueryResultPage<Comment> commentQueryResultPage = mapper.queryPage(Comment.class, query);
        return commentQueryResultPage.getResults();
    }

    public void deleteChildComment(List<Comment> childCommentList) {
        for (Comment comment : childCommentList) {
            comment.setStatus(DELETED);
        }
        mapper.batchSave(childCommentList);
    }

    public Comment findCommentByCommentSK(String postPK, String commentSK) throws BaseException {
        /*
        HashMap<String, String> ean = new HashMap<>();
        ean.put("#PK", "PartitionKey");
        ean.put("#SK", "SortKey");

        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":val1", new AttributeValue().withS(postPK));
        eav.put(":val2", new AttributeValue().withS(commentSK));

        DynamoDBQueryExpression<Comment> query = new DynamoDBQueryExpression<Comment>()
                .withKeyConditionExpression("#PK = :val1 and #SK = :val2")
                .withExpressionAttributeValues(eav)
                .withExpressionAttributeNames(ean);

        List<Comment> comments = mapper.query(Comment.class, query);

        if (comments.isEmpty()) {
            throw new BaseException(INVALID_COMMENT_ID);
        }
        return comments.get(0);
         */
        return commentCrudRepository.findCommentByPostPKAndUploadedDateStartingWith(postPK, commentSK)
                .orElseThrow(() -> new BaseException(INVALID_COMMENT_SK));
    }

    @Data
    @NoArgsConstructor
    public static class CommentData {
        private List<Comment> result;
        private Map<String, String> exclusiveStartKey;

        public CommentData(List<Comment> result, Map<String, String> resultLastEvaluatedKey) {
            this.result = result;
            this.exclusiveStartKey = resultLastEvaluatedKey;
        }
    }
}