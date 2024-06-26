package com.apps.pochak.member.service;

import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.global.s3.S3Service;
import com.apps.pochak.login.jwt.JwtService;
import com.apps.pochak.member.dto.request.ProfileUpdateRequest;
import com.apps.pochak.member.dto.response.ProfileUpdateResponse;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.member.dto.response.MemberElements;
import com.apps.pochak.member.dto.response.ProfileResponse;
import com.apps.pochak.post.domain.Post;
import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.*;
import static com.apps.pochak.global.s3.DirName.MEMBER;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.post.dto.PostElements;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;
    private final JwtService jwtService;
    private final S3Service awsS3Service;

    @Transactional(readOnly = true)
    public ProfileResponse getProfileDetail(final String handle,
                                            final Pageable pageable
    ) {
        final Member loginMember = jwtService.getLoginMember();
        final Member member = memberRepository.findByHandle(handle, loginMember);
        final long followerCount = followRepository.countActiveFollowByReceiver(member);
        final long followingCount = followRepository.countActiveFollowBySender(member);
        final Page<Post> taggedPost = postRepository.findTaggedPost(member, loginMember, pageable);
        final Boolean isFollow = (handle.equals(loginMember.getHandle())) ?
                null : followRepository.existsBySenderAndReceiver(loginMember, member);

        return ProfileResponse.of()
                .member(member)
                .postPage(taggedPost)
                .followerCount(followerCount)
                .followingCount(followingCount)
                .isFollow(isFollow)
                .build();
    }

    public ProfileUpdateResponse updateProfileDetail(final String handle,
                                                     final ProfileUpdateRequest profileUpdateRequest){
        final Member loginMember = jwtService.getLoginMember();
        final Member updateMember = memberRepository.findByHandleWithoutLogin(handle);
        if (!loginMember.equals(updateMember)) {
            throw new GeneralException(UNAUTHORIZED_MEMBER_REQUEST);
        }
        String profileImageUrl = updateMember.getProfileImage();
        if (profileUpdateRequest.getProfileImage() != null) {
            awsS3Service.deleteFileFromS3(updateMember.getProfileImage());
            profileImageUrl = awsS3Service.upload(profileUpdateRequest.getProfileImage(), MEMBER);
        }
        updateMember.updateMember(profileUpdateRequest, profileImageUrl);

        return ProfileUpdateResponse.builder()
                .name(updateMember.getName())
                .handle(updateMember.getHandle())
                .message(updateMember.getMessage())
                .profileImage(updateMember.getProfileImage())
                .build();
    }

    @Transactional(readOnly = true)
    public PostElements getTaggedPosts(
            final String handle,
            final Pageable pageable
    ) {
        final Member loginMember = jwtService.getLoginMember();
        final Member member = memberRepository.findByHandle(handle, loginMember);
        final Page<Post> taggedPost = postRepository.findTaggedPost(member, loginMember, pageable);
        return PostElements.from(taggedPost);
    }

    @Transactional(readOnly = true)
    public PostElements getUploadPosts(
            final String handle,
            final Pageable pageable
    ) {
        final Member loginMember = jwtService.getLoginMember();
        final Member owner = memberRepository.findByHandle(handle, loginMember);
        final Page<Post> taggedPost = postRepository.findUploadPost(owner, loginMember, pageable);
        return PostElements.from(taggedPost);
    }

    @Transactional(readOnly = true)
    public MemberElements search(
            final String keyword,
            final Pageable pageable
    ) {
        Member loginMember = jwtService.getLoginMember();
        Page<Member> memberPage = memberRepository.searchByKeyword(keyword, loginMember, pageable);
        return MemberElements.from(memberPage);
    }

    private Member findMemberByHandle(final String handle, final Member loginMember) {
        return memberRepository.findByHandle(handle, loginMember);
    }

    @Transactional(readOnly = true)
    public void checkDuplicate(String handle) {
        memberRepository.checkDuplicateHandle(handle);
    }
}

