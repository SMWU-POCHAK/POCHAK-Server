package com.apps.pochak.report.domain;

import com.apps.pochak.global.BaseEntity;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@SQLDelete(sql = "UPDATE report SET status = 'DELETED' WHERE id = ?")
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Enumerated(STRING)
    private ReportType reportType;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "reported_post_id")
    private Post reportedPost;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "reporter_id")
    private Member reporter;

    @Builder
    public Report(
            final ReportType reportType,
            final Post reportedPost,
            final Member reporter
    ) {
        this.reportType = reportType;
        this.reportedPost = reportedPost;
        this.reporter = reporter;
    }
}
