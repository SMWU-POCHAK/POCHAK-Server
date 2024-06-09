package com.apps.pochak.member.service;

import com.amazonaws.services.ec2.util.S3UploadPolicy;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.global.s3.S3Service;
import com.apps.pochak.login.jwt.JwtService;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.member.dto.request.ProfileUpdateRequest;
import com.apps.pochak.member.dto.response.ProfileResponse;
import com.apps.pochak.member.dto.response.ProfileUpdateResponse;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.post.dto.PostElements;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.DUPLICATE_HANDLE;
import static com.apps.pochak.global.s3.DirName.MEMBER;
import static com.apps.pochak.post.domain.PostStatus.PUBLIC;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;
    private final JwtService jwtService;
    private final S3Service awsS3Service;

    public ProfileResponse getProfileDetail(final String handle,
                                            final Pageable pageable
    ) {
        final String loginMemberHandle = jwtService.getLoginMemberHandle();
        final Member loginMember = jwtService.getLoginMember();
        final Member member = findMemberByHandle(handle);
        final long followerCount = followRepository.countActiveFollowByReceiver(member);
        final long followingCount = followRepository.countActiveFollowBySender(member);
        final Page<Post> taggedPost = postRepository.findTaggedPost(member, pageable);
        final Boolean isFollow = (handle.equals(loginMemberHandle)) ?
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
        final Member member = findMemberByHandle(handle);
        checkHandleDuplication(handle);
        String profileImageUrl = awsS3Service.upload(profileUpdateRequest.getProfieImage(), MEMBER);
        member.updateMember(profileUpdateRequest, profileImageUrl);

        return ProfileUpdateResponse.builder()
                .name(member.getName())
                .handle(member.getHandle())
                .message(member.getMessage())
                .profileImage(member.getProfileImage())
                .build();
    }

    public PostElements getTaggedPosts(
            final String handle,
            final Pageable pageable
    ) {
        final Member member = findMemberByHandle(handle);
        final Page<Post> taggedPost = postRepository.findTaggedPost(member, pageable);
        return PostElements.from(taggedPost);
    }

    public PostElements getUploadPosts(
            final String handle,
            final Pageable pageable
    ) {
        final Member member = findMemberByHandle(handle);
        final Page<Post> taggedPost;
        if (isMyProfile(member)) {
            taggedPost = postRepository.findPostByOwnerOrderByCreatedDateDesc(member, pageable);
        } else {
            taggedPost = postRepository.findPostByOwnerAndPostStatusOrderByCreatedDateDesc(member, PUBLIC, pageable);
        }
        return PostElements.from(taggedPost);
    }

    private Boolean isMyProfile(final Member member) {
        final String loginMemberHandle = jwtService.getLoginMemberHandle();
        return member.getHandle().equals(loginMemberHandle);
    }

    private Member findMemberByHandle(final String handle) {
        return memberRepository.findByHandle(handle);
    }

    private void checkHandleDuplication(String handle) {
        if (memberRepository.existsByHandle(handle)) {
            throw new GeneralException(DUPLICATE_HANDLE);
        }
    }
}


