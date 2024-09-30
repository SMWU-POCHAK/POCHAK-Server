package com.apps.pochak.report.service;

import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.login.provider.JwtProvider;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
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
    private final MemberRepository memberRepository;

    public void saveReport(
            final Accessor accessor,
            final ReportUploadRequest request
    ) {
        final Member reporter = memberRepository.findMemberById(accessor.getMemberId());
        final Post reportedPost = postRepository.findPostById(request.getPostId());

        Report report = request.toEntity(reporter, reportedPost);
        reportRepository.save(report);
    }
}
