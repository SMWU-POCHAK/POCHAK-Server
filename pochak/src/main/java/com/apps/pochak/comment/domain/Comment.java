package com.apps.pochak.comment.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.apps.pochak.common.BaseEntity;

import com.apps.pochak.user.domain.User;
import lombok.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel.DynamoDBAttributeType;

@NoArgsConstructor

@AllArgsConstructor


@DynamoDBTable(tableName = "pochakdatabase")
public class Comment extends BaseEntity {
    @Id
    private CommentId commentId;

    private String postPK;

    private String uploadedDate;

    @DynamoDBAttribute
    @Getter
    @Setter
    private String commentUserHandle;

    @DynamoDBAttribute
    @Getter
    @Setter
    @DynamoDBTyped(DynamoDBAttributeType.L)
    private List<String> childCommentSKs = new ArrayList<>();

    @DynamoDBAttribute
    @Getter
    @Setter
    private String content;

    @Builder
    public Comment(String postPK,User loginUser,List<String> childCommentSKs,String content,String uploadedDate){
        this.postPK=postPK;
        this.commentUserHandle=loginUser.getHandle();
        this.childCommentSKs=childCommentSKs;
        this.content=content;
        this.uploadedDate=uploadedDate;
    }

    @DynamoDBHashKey(attributeName = "PartitionKey")
    public String getPostPK() {
        return commentId != null ? commentId.getPostPK() : null;
    }

    public void setPostPK(String postPK) {
        if (commentId == null) {
            commentId = new CommentId();
        }
        commentId.setPostPK(postPK);
    }

    @DynamoDBRangeKey(attributeName = "SortKey")
    public String getUploadedDate() {
        return commentId != null ? commentId.getUploadedDate() : null;
    }

    /**
     * 사용 유의! 앞에 Prefix(COMMENT#) 붙어있어야 함.
     * @param uploadedDate
     */
    public void setUploadedDate(String uploadedDate) {
        if (commentId == null) {
            commentId = new CommentId();
        }
        commentId.setUploadedDate(uploadedDate);
    }

    public void setUploadedDate(LocalDateTime uploadedDate) {
        if (commentId == null) {
            commentId = new CommentId();
        }
        commentId.setUploadedDate("COMMENT#" + uploadedDate);
    }
}
