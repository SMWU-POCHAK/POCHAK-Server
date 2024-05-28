package com.apps.pochak.report.dto.request;

import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.report.domain.Report;
import com.apps.pochak.report.domain.ReportType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportUploadRequest {
    @NotNull(message = "신고된 게시글 아이디는 필수로 전달해야 합니다.")
    private Long postId;

    @NotNull(message = "신고 유형은 필수로 전달해야 합니다.")
    private ReportType reportType;

    public Report toEntity(
            final Member reporter,
            final Post reportedPost
    ) {
        return Report.builder()
                .reportType(reportType)
                .reportedPost(reportedPost)
                .reporter(reporter)
                .build();
    }
}
