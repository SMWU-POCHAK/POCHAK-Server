package com.apps.pochak.elastic.like;

import com.apps.pochak.global.BaseDocument;
import com.apps.pochak.like.domain.LikeEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import static lombok.AccessLevel.PROTECTED;

@Document(indexName = "#{@likeIndex}")
@Getter
@DynamicInsert
@NoArgsConstructor(access = PROTECTED)
public class LikeDocument extends BaseDocument {
    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String memberId;

    @Field(type = FieldType.Keyword)
    private String postId;

    @Override
    public boolean isNew() {
        return id == null || (getCreatedDate() == null);
    }

    @Builder(builderMethodName = "from")
    public LikeDocument(LikeEntity likeEntity) {
        this.id = likeEntity.getId().toString();
        this.memberId = likeEntity.getLikeMember().getId().toString();
        this.postId = likeEntity.getLikedPost().getId().toString();
    }
}
