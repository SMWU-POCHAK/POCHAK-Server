package com.apps.pochak.report.service;

import com.apps.pochak.login.provider.JwtProvider;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.report.domain.Report;
import com.apps.pochak.report.domain.repository.ReportRepository;
import com.apps.pochak.report.dto.request.ReportUploadRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final JwtProvider jwtProvider;

    public void saveReport(final ReportUploadRequest request) {
        final Member reporter = jwtProvider.getLoginMember();
        final Post reportedPost = postRepository.findPostById(request.getPostId(), reporter);

        Report report = request.toEntity(reporter, reportedPost);
        reportRepository.save(report);
    }
}
