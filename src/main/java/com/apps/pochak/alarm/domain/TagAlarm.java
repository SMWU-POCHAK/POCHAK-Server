package com.apps.pochak.alarm.domain;

import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.tag.domain.Tag;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import static com.apps.pochak.alarm.domain.AlarmType.TAG_APPROVAL;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@DynamicInsert
@DiscriminatorValue("TAG_ALARM")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TagAlarm extends Alarm {

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "tag_approval_id")
    private Tag tag;

    private Long taggerId;
    private String taggerHandle;
    private String taggerName;
    private String taggerProfileImage;

    private Long postId;
    private String postImage;

    public TagAlarm(
            final Tag tag,
            final Member receiver
    ) {
        super(receiver, TAG_APPROVAL);
        this.tag = tag;

        Member tagger = tag.getMember();
        this.taggerId = tagger.getId();
        this.taggerHandle = tagger.getHandle();
        this.taggerName = tagger.getName();
        this.taggerProfileImage = tagger.getProfileImage();

        Post post = tag.getPost();
        this.postId = post.getId();
        this.postImage = post.getPostImage();
    }
}
