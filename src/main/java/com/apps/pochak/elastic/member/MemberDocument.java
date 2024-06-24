package com.apps.pochak.elastic.member;

import com.apps.pochak.global.BaseDocument;
import com.apps.pochak.member.domain.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import static lombok.AccessLevel.PROTECTED;

@Document(indexName = "#{@memberIndex}")
@Getter
@DynamicInsert
@NoArgsConstructor(access = PROTECTED)
public class MemberDocument extends BaseDocument {
    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String handle;

    @Field(type = FieldType.Text)
    private String profileImage;

    @Override
    public boolean isNew() {
        return id == null || (getCreatedDate() == null);
    }

    @Builder(builderMethodName = "from")
    public MemberDocument(Member member) {
        this.id = member.getId().toString();
        this.handle = member.getHandle();
        this.profileImage = member.getProfileImage();
    }
}
