package com.apps.pochak.login.oauth;

import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import com.apps.pochak.comment.domain.repository.CommentRepository;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.global.s3.S3Service;
import com.apps.pochak.like.domain.repository.LikeRepository;
import com.apps.pochak.login.dto.request.MemberInfoRequest;
import com.apps.pochak.login.dto.response.OAuthMemberResponse;
import com.apps.pochak.login.jwt.JwtService;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.SocialType;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.tag.domain.repository.TagRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.EXIST_USER;
import static com.apps.pochak.global.s3.DirName.MEMBER;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final JwtService jwtService;
    private final AlarmRepository alarmRepository;
    private final CommentRepository commentRepository;
    private final FollowRepository followRepository;
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final MemberRepository memberRepository;
    private final AppleOAuthService appleOAuthService;
    private final S3Service awsS3Service;

    @Transactional
    public OAuthMemberResponse signup(MemberInfoRequest memberInfoRequest) {
        Optional<Member> findMember = memberRepository.findMemberBySocialId(memberInfoRequest.getSocialId());

        if (findMember.isPresent()) {
            throw new GeneralException(EXIST_USER); // TODO: Exception이 아니라 Login으로 넘어가야할 듯
        }

        String profileImageUrl = awsS3Service.upload(memberInfoRequest.getProfileImage(), MEMBER);

        String refreshToken = jwtService.createRefreshToken();
        String accessToken = jwtService.createAccessToken(memberInfoRequest.getHandle());

        Member member = Member.signupMember()
                .name(memberInfoRequest.getName())
                .email(memberInfoRequest.getEmail())
                .handle(memberInfoRequest.getHandle())
                .message(memberInfoRequest.getMessage())
                .socialId(memberInfoRequest.getSocialId())
                .profileImage(profileImageUrl)
                .socialType(SocialType.of(memberInfoRequest.getSocialType()))
                .socialRefreshToken(memberInfoRequest.getSocialRefreshToken())
                .build();

        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        return OAuthMemberResponse.builder()
                .isNewMember(false)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public void logout(final String handle) {
        final Member member = memberRepository.findByHandleWithoutLogin(handle);
        member.updateRefreshToken(null);
        memberRepository.save(member);
    }

    @Transactional
    public void signout(final String handle) {
        final Member member = memberRepository.findByHandleWithoutLogin(handle);
        if (member.getSocialType().equals(SocialType.APPLE)) {
            appleOAuthService.revoke(member.getRefreshToken());
        }
        alarmRepository.deleteAlarmByMemberId(member.getId());
        commentRepository.deleteCommentByMemberId(member.getId());
        followRepository.deleteFollowByMemberId(member.getId());
        likeRepository.deleteLikeByMemberId(member.getId());
        tagRepository.deleteTagByMemberId(member.getId());
        postRepository.deletePostByMemberId(member.getId());
        memberRepository.deleteMemberByMemberId(member.getId());
    }
}
