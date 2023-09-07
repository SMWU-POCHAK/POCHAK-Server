package com.apps.pochak.post.controller;

import com.apps.pochak.comment.dto.CommentResDto;
import com.apps.pochak.comment.dto.CommentUploadRequestDto;
import com.apps.pochak.comment.service.CommentService;
import com.apps.pochak.common.BaseException;
import com.apps.pochak.common.BaseResponse;
import com.apps.pochak.login.jwt.JwtHeaderUtil;
import com.apps.pochak.login.jwt.JwtService;
import com.apps.pochak.post.dto.PostDetailResDto;
import com.apps.pochak.post.dto.PostUploadRequestDto;
import com.apps.pochak.post.dto.PostUploadResDto;
import com.apps.pochak.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.apps.pochak.common.BaseResponseStatus.NULL_COMMENTS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/post")
public class PostController {
    private final PostService postService;
    private final CommentService commentService;
    private final JwtService jwtService;

    // TODO: param으로 받은 로그인 정보 추후 수정 필요
    // post 저장 api
    @PostMapping("")
    public BaseResponse<PostUploadResDto> savePost(@RequestBody PostUploadRequestDto requestDto,
                                                   @RequestParam("loginUser") String loginUserHandle) {
        try {
            return new BaseResponse<>(postService.savePost(requestDto, loginUserHandle));
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

    // post detail 가져오는 api
    @GetMapping("/{postPK}")
    public BaseResponse<PostDetailResDto> findPostDetailByPostId(@PathVariable("postPK") String postPK,
                                                                 @RequestParam("loginUser") String loginUserHandle) {
        try {
            PostDetailResDto postDetail = postService.getPostDetail(postPK, loginUserHandle);
            if (postDetail.getMainComment() == null) {
                return new BaseResponse<>(postDetail, NULL_COMMENTS);
            }
            return new BaseResponse<>(postDetail);
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

    // postPK를 포함해서 요청 받음
    // SK가 COMMENT#PARENT#(생성날짜)
    @PostMapping("/{postPK}/comment")
    public BaseResponse<CommentResDto> commentUpload(@PathVariable("postPK") String postPK,
                                                     @RequestBody CommentUploadRequestDto requestDto) {
        try {
            // login
            String accessToken = JwtHeaderUtil.getAccessToken();
            String loginUserHandle = jwtService.getHandle(accessToken);

            // 부모 comment인지, 자식 comment 인지 분류 -> 분류 로직 service로 이동
            return new BaseResponse<>(commentService.commentUpload(
                    postPK,
                    requestDto,
                    loginUserHandle,
                    requestDto.getParentCommentSK()));

        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }


    // 좋아요 누르기 api -
    @PostMapping("/{postPK}/like")
    public BaseResponse likePost(@PathVariable("postPK") String postPK,
                                 @RequestParam("loginUser") String loginUserHandle) {
        try {
            return new BaseResponse<>(postService.likePost(postPK, loginUserHandle));
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }

    }

    @DeleteMapping("/{postPK}")
    public BaseResponse deletePost(@PathVariable("postPK") String postPK) {
        try {
            // login
            String accessToken = JwtHeaderUtil.getAccessToken();
            String loginUserHandle = jwtService.getHandle(accessToken);

            return new BaseResponse<>(postService.deletePost(postPK, loginUserHandle));
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }
}
