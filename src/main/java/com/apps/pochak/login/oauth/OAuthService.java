package com.apps.pochak.login.oauth;

import com.apps.pochak.comment.domain.repository.CommentRepository;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.global.api_payload.exception.handler.InvalidJwtException;
import com.apps.pochak.global.s3.S3Service;
import com.apps.pochak.like.domain.repository.LikeRepository;
import com.apps.pochak.login.dto.request.MemberInfoRequest;
import com.apps.pochak.login.dto.response.OAuthMemberResponse;
import com.apps.pochak.login.dto.response.PostTokenResponse;
import com.apps.pochak.login.jwt.JwtHeaderUtil;
import com.apps.pochak.login.jwt.JwtService;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.SocialType;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.tag.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.*;
import static com.apps.pochak.global.s3.DirName.MEMBER;

@Service
@RequiredArgsConstructor
@Transactional
public class OAuthService {
    private final JwtService jwtService;
    private final CommentRepository commentRepository;
    private final FollowRepository followRepository;
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final MemberRepository memberRepository;
    private final AppleOAuthService appleOAuthService;
    private final S3Service awsS3Service;

    public OAuthMemberResponse signup(MemberInfoRequest memberInfoRequest) {
        SocialType socialType = SocialType.of(memberInfoRequest.getSocialType());
        Optional<Member> findMember = memberRepository.findMemberBySocialIdAndSocialType(memberInfoRequest.getSocialId(), socialType);
        if (findMember.isPresent()) throw new GeneralException(EXIST_USER);


        String profileImageUrl = awsS3Service.upload(memberInfoRequest.getProfileImage(), MEMBER);
        String refreshToken = jwtService.createRefreshToken();

        Member member = Member.signupMember()
                .name(memberInfoRequest.getName())
                .email(memberInfoRequest.getEmail())
                .handle(memberInfoRequest.getHandle())
                .message(memberInfoRequest.getMessage())
                .socialId(memberInfoRequest.getSocialId())
                .profileImage(profileImageUrl)
                .refreshToken(refreshToken)
                .socialType(socialType)
                .socialRefreshToken(memberInfoRequest.getSocialRefreshToken())
                .build();
        memberRepository.save(member);

        String accessToken = jwtService.createAccessToken(member.getId().toString());

        return new OAuthMemberResponse(member, false, accessToken);
    }

    @Transactional(readOnly = true)
    public PostTokenResponse reissueAccessToken() {
        String accessToken = JwtHeaderUtil.getAccessToken();
        String refreshToken = JwtHeaderUtil.getRefreshToken();
        if (jwtService.isValidRefreshAndInvalidAccess(refreshToken, accessToken)) {
            String id = jwtService.getSubject(accessToken);
            Member member = memberRepository.findMemberByIdAndRefreshToken(Long.parseLong(id), refreshToken)
                    .orElseThrow(() -> new InvalidJwtException(INVALID_REFRESH_TOKEN));
            return PostTokenResponse.builder()
                    .accessToken(jwtService.createAccessToken(member.getId().toString()))
                    .build();
        }
        if (jwtService.isValidRefreshAndValidAccess(refreshToken, accessToken)) {
            return PostTokenResponse.builder()
                    .accessToken(jwtService.createAccessToken(accessToken))
                    .build();
        }
        throw new InvalidJwtException(FAIL_VALIDATE_TOKEN);
    }

    public void logout(final String id) {
        final Member member = memberRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new GeneralException(INVALID_ACCESS_TOKEN));
        member.updateRefreshToken(null);
    }

    public void signout(final String id) {
        final Member member = memberRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new GeneralException(INVALID_ACCESS_TOKEN));
        if (member.getSocialType().equals(SocialType.APPLE)) {
            appleOAuthService.revoke(member.getRefreshToken());
        }

        List<Long> postIdList = postRepository.findPostIdListByOwnerOrTaggedMember(member);
        commentRepository.deleteCommentByMemberOrPostList(member.getId(), postIdList);
        followRepository.deleteFollowByMember(member.getId());
        likeRepository.deleteLikeByMemberOrPostList(member.getId(), postIdList);
        tagRepository.deleteTagByMemberOrPostList(member.getId(), postIdList);
        postRepository.deleteAllPost(postIdList);
        memberRepository.deleteMemberByMemberId(member.getId());
    }
}
