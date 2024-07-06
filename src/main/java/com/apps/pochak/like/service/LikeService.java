package com.apps.pochak.like.service;

import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import com.apps.pochak.alarm.service.LikeAlarmService;
import com.apps.pochak.like.domain.LikeEntity;
import com.apps.pochak.like.domain.repository.LikeRepository;
import com.apps.pochak.like.dto.response.LikeElement;
import com.apps.pochak.like.dto.response.LikeElements;
import com.apps.pochak.login.provider.JwtProvider;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.tag.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.apps.pochak.global.BaseEntityStatus.ACTIVE;
import static com.apps.pochak.global.BaseEntityStatus.DELETED;

@Service
@Transactional
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final AlarmRepository alarmRepository;
    private final LikeAlarmService likeAlarmService;
    private final JwtProvider jwtProvider;

    public void likePost(final Long postId) {
        final Member loginMember = jwtProvider.getLoginMember();
        final Post post = postRepository.findPostById(postId, loginMember);

        final Optional<LikeEntity> optionalLike = likeRepository.findByLikeMemberAndLikedPost(loginMember, post);
        if (optionalLike.isPresent()) {
            final LikeEntity postLike = optionalLike.get();
            toggleLikeStatus(postLike);
        } else {
            saveNewLikeEntity(
                    loginMember,
                    post
            );
        }
    }

    private void toggleLikeStatus(LikeEntity like) {
        if (like.getStatus().equals(ACTIVE)) {
            like.setStatus(DELETED);
            likeAlarmService.deleteAlarmByLike(like);
        } else {
            like.setStatus(ACTIVE);
            likeAlarmService.sendLikeAlarm(like, like.getLikedPost().getOwner());
        }
    }

    private void saveNewLikeEntity(
            final Member loginMember,
            final Post post
    ) {
        final LikeEntity like = LikeEntity.builder()
                .likeMember(loginMember)
                .likedPost(post)
                .build();
        likeRepository.save(like);
        likeAlarmService.sendLikeAlarm(like, post.getOwner());
    }

    @Transactional(readOnly = true)
    public LikeElements getMemberLikedPost(final Long postId) {
        final Member loginMember = jwtProvider.getLoginMember();
        final Post likedPost = postRepository.findPostById(postId, loginMember);

        final List<LikeElement> likeElements = likeRepository.findLikesAndIsFollow(
                loginMember.getId(),
                likedPost
        );

        return new LikeElements(likeElements);
    }
}
