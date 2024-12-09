package com.apps.pochak.post.controller;

import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.comment.domain.Comment;
import com.apps.pochak.global.ControllerTest;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.dto.PostElements;
import com.apps.pochak.post.dto.response.PostDetailResponse;
import com.apps.pochak.post.service.PostService;
import com.apps.pochak.tag.domain.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;

import java.util.List;

import static com.apps.pochak.comment.fixture.CommentFixture.STATIC_CHILD_COMMENT;
import static com.apps.pochak.global.ApiDocumentUtils.getDocumentRequest;
import static com.apps.pochak.global.ApiDocumentUtils.getDocumentResponse;
import static com.apps.pochak.global.MockMultipartFileConverter.getMockMultipartFileOfPost;
import static com.apps.pochak.global.converter.ListToPageConverter.toPage;
import static com.apps.pochak.member.fixture.MemberFixture.STATIC_MEMBER1;
import static com.apps.pochak.post.fixture.PostFixture.STATIC_PUBLIC_POST;
import static com.apps.pochak.tag.fixture.TagFixture.STATIC_APPROVED_TAG;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureRestDocs
@WebMvcTest(PostController.class)
@MockBean(JpaMetamodelMappingContext.class)
class PostControllerTest extends ControllerTest {
    private static final Member MEMBER1 = STATIC_MEMBER1;
    private static final Comment CHILD_COMMENT = STATIC_CHILD_COMMENT;
    private static final Post PUBLIC_POST = STATIC_PUBLIC_POST;
    private static final Tag APPROVED_TAG = STATIC_APPROVED_TAG;

    @MockBean
    PostService postService;

    @BeforeEach
    void setUp() {
        given(jwtProvider.validateAccessToken(any())).willReturn(true);
        given(jwtProvider.getSubject(any())).willReturn(MEMBER1.getId().toString());
        given(loginArgumentResolver.resolveArgument(any(), any(), any(), any()))
                .willReturn(Accessor.member(MEMBER1.getId()));
    }

    @Test
    @DisplayName("홈 탭을 조회한다.")
    void getHomeTab() throws Exception {
        when(postService.getHomeTab(any(), any()))
                .thenReturn(PostElements.from(toPage(List.of(PUBLIC_POST))));

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v2/posts")
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("get-home-tab",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization").description("Basic auth credentials")
                                ),
                                queryParameters(
                                        parameterWithName("page").description("조회할 페이지 [default: 0]").optional()
                                ),
                                responseFields(
                                        fieldWithPath("isSuccess").type(BOOLEAN).description("성공 여부"),
                                        fieldWithPath("code").type(STRING).description("결과 코드"),
                                        fieldWithPath("message").type(STRING).description("결과 메세지"),
                                        fieldWithPath("result").type(OBJECT).description("결과 데이터"),
                                        fieldWithPath("result.pageInfo").type(OBJECT).description("게시물 페이징 정보"),
                                        fieldWithPath("result.pageInfo.lastPage").type(BOOLEAN)
                                                .description(
                                                        "게시물 페이징 정보: 현재 페이지가 마지막 페이지인지의 여부"
                                                ),
                                        fieldWithPath("result.pageInfo.totalPages").type(NUMBER)
                                                .description(
                                                        "게시물 페이징 정보: 총 페이지 수"
                                                ),
                                        fieldWithPath("result.pageInfo.totalElements").type(NUMBER)
                                                .description(
                                                        "게시물 페이징 정보: 태그된 총 포스트 수"
                                                ),
                                        fieldWithPath("result.pageInfo.size").type(NUMBER)
                                                .description(
                                                        "게시물 페이징 정보: 페이징 사이즈"
                                                ),
                                        fieldWithPath("result.postList").type(ARRAY).description("게시물 리스트"),
                                        fieldWithPath("result.postList[].postId").type(NUMBER)
                                                .description("게시물 리스트: 게시물 아이디").optional(),
                                        fieldWithPath("result.postList[].postImage").type(STRING)
                                                .description("게시물 리스트: 게시물 이미지").optional()
                                )
                        )
                );
    }

    @Test
    @DisplayName("탐색탭을 조회한다.")
    void getSearchTab() throws Exception {
        when(postService.getSearchTab(any(), any()))
                .thenReturn(PostElements.from(toPage(List.of(PUBLIC_POST))));

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v2/posts/search")
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("get-search-tab",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization").description("Basic auth credentials")
                                ),
                                queryParameters(
                                        parameterWithName("page").description("조회할 페이지 [default: 0]").optional()
                                ),
                                responseFields(
                                        fieldWithPath("isSuccess").type(BOOLEAN).description("성공 여부"),
                                        fieldWithPath("code").type(STRING).description("결과 코드"),
                                        fieldWithPath("message").type(STRING).description("결과 메세지"),
                                        fieldWithPath("result").type(OBJECT).description("결과 데이터"),
                                        fieldWithPath("result.pageInfo").type(OBJECT).description("게시물 페이징 정보"),
                                        fieldWithPath("result.pageInfo.lastPage").type(BOOLEAN)
                                                .description(
                                                        "게시물 페이징 정보: 현재 페이지가 마지막 페이지인지의 여부"
                                                ),
                                        fieldWithPath("result.pageInfo.totalPages").type(NUMBER)
                                                .description(
                                                        "게시물 페이징 정보: 총 페이지 수"
                                                ),
                                        fieldWithPath("result.pageInfo.totalElements").type(NUMBER)
                                                .description(
                                                        "게시물 페이징 정보: 태그된 총 포스트 수"
                                                ),
                                        fieldWithPath("result.pageInfo.size").type(NUMBER)
                                                .description(
                                                        "게시물 페이징 정보: 페이징 사이즈"
                                                ),
                                        fieldWithPath("result.postList").type(ARRAY).description("게시물 리스트"),
                                        fieldWithPath("result.postList[].postId").type(NUMBER)
                                                .description("게시물 리스트: 게시물 아이디"),
                                        fieldWithPath("result.postList[].postImage").type(STRING)
                                                .description("게시물 리스트: 게시물 이미지")
                                )
                        )
                );
    }

    @Test
    @DisplayName("게시물을 업로드한다.")
    void uploadPost() throws Exception {
        String caption = "안녕하세요. 게시물 업로드를 테스트해보겠습니다.";
        final List<String> taggedMemberHandles = List.of(MEMBER1.getHandle());

        doNothing().when(postService).savePost(any(), any());

        this.mockMvc.perform(
                        multipart("/api/v2/posts")
                                .file(getMockMultipartFileOfPost())
                                .queryParam("taggedMemberHandleList", String.join(", ", taggedMemberHandles))
                                .queryParam("caption", caption)
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
                ).andExpect(status().isOk())
                .andDo(
                        document("upload-post",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization").description("Basic auth credentials")
                                ),
                                requestParts(
                                        partWithName("postImage").description("업로드 할 게시물 사진 파일 : 빈 파일 전달 시 에러 발생")
                                ),
                                queryParameters(
                                        parameterWithName("taggedMemberHandleList").description("태그된 멤버들의 아이디(handle) 리스트"),
                                        parameterWithName("caption").description("게시물 내용")
                                ),
                                responseFields(
                                        fieldWithPath("isSuccess").type(BOOLEAN).description("성공 여부"),
                                        fieldWithPath("code").type(STRING).description("결과 코드"),
                                        fieldWithPath("message").type(STRING).description("결과 메세지")
                                )

                        )
                );
    }

    @Test
    @DisplayName("게시물 상세 페이지를 조회한다.")
    void getPostDetail() throws Exception {
        when(postService.getPostDetail(any(), any()))
                .thenReturn(PostDetailResponse.of()
                        .post(PUBLIC_POST)
                        .tagList(List.of(APPROVED_TAG))
                        .isFollow(true)
                        .isLike(true)
                        .likeCount(5)
                        .recentComment(CHILD_COMMENT)
                        .build()
                );

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v2/posts/{postId}", PUBLIC_POST.getId())
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("get-detail-post",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization")
                                                .description(
                                                        "Basic auth credentials  \n" +
                                                                ": 만약 아직 수락된 게시물이 아니라면 게시자와 태그된 사람만 접근 가능합니다."
                                                )
                                ),
                                pathParameters(
                                        parameterWithName("postId").description("게시물 아이디")
                                ),
                                responseFields(
                                        fieldWithPath("isSuccess").type(BOOLEAN).description("성공 여부"),
                                        fieldWithPath("code").type(STRING).description("결과 코드"),
                                        fieldWithPath("message").type(STRING).description("결과 메세지"),
                                        fieldWithPath("result").type(OBJECT).description("결과 데이터"),
                                        fieldWithPath("result.ownerId").type(NUMBER).description("게시자 아이디"),
                                        fieldWithPath("result.ownerHandle").type(STRING).description("게시자 핸들 (handle)"),
                                        fieldWithPath("result.ownerProfileImage").type(STRING).description("게시자 프로필 이미지"),
                                        fieldWithPath("result.tagList").type(ARRAY).description("태그된 유저 리스트"),
                                        fieldWithPath("result.tagList[].memberId").type(NUMBER).description("태그된 리스트 | 유저 아이디"),
                                        fieldWithPath("result.tagList[].profileImage").type(STRING).description("태그된 리스트 | 유저 프로필 이미지"),
                                        fieldWithPath("result.tagList[].handle").type(STRING).description("태그된 리스트 | 유저 핸들"),
                                        fieldWithPath("result.tagList[].name").type(STRING).description("태그된 리스트 | 유저 이름"),
                                        fieldWithPath("result.isFollow").type(BOOLEAN)
                                                .description(
                                                        "현재 로그인한 유저가 게시자를 팔로우하고 있는지 여부 \n" +
                                                                ": 만약 로그인한 유저가 게시자라면 null로 전달됨."
                                                ),
                                        fieldWithPath("result.postImage").type(STRING).description("게시물 이미지 URL"),
                                        fieldWithPath("result.isLike").type(BOOLEAN)
                                                .description(
                                                        "현재 로그인한 유저가 해당 게시물의 좋아요를 눌렀는지 여부"
                                                ),
                                        fieldWithPath("result.likeCount").type(NUMBER).description("게시물의 좋아요 개수"),
                                        fieldWithPath("result.caption").type(STRING).description("게시물의 caption"),
                                        fieldWithPath("result.recentComment").type(OBJECT)
                                                .description(
                                                        "게시물의 가장 최근 댓글 : 댓글이 없는 경우 NULL이 전달됨."
                                                ),
                                        fieldWithPath("result.recentComment.commentId").type(NUMBER)
                                                .description(
                                                        "게시물의 가장 최근 댓글 : 댓글 아이디"
                                                ).optional(),
                                        fieldWithPath("result.recentComment.memberId").type(NUMBER)
                                                .description(
                                                        "게시물의 가장 최근 댓글 : 댓글 작성자 아이디"
                                                ).optional(),
                                        fieldWithPath("result.recentComment.profileImage").type(STRING)
                                                .description(
                                                        "게시물의 가장 최근 댓글 : 댓글 게시자의 프로필 이미지"
                                                ).optional(),
                                        fieldWithPath("result.recentComment.handle").type(STRING)
                                                .description(
                                                        "게시물의 가장 최근 댓글 : 댓글 게시자의 핸들 (handle)"
                                                ).optional(),
                                        fieldWithPath("result.recentComment.createdDate").type(STRING)
                                                .description(
                                                        "게시물의 가장 최근 댓글 : 댓글 게시 시간"
                                                ).optional(),
                                        fieldWithPath("result.recentComment.content").type(STRING)
                                                .description(
                                                        "게시물의 가장 최근 댓글 : 댓글 내용"
                                                ).optional()
                                )

                        )
                );
    }

    @Test
    @DisplayName("게시글을 삭제한다.")
    void deletePostTest() throws Exception {
        doNothing().when(postService).deletePost(any(), any());

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .delete("/api/v2/posts/{postId}", PUBLIC_POST.getId())
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("delete-post",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization")
                                                .description(
                                                        "Basic auth credentials  \n" +
                                                                ": 만약 자신의 게시물이 아니라면 권한 에러가 발생합니다."
                                                )
                                ),
                                pathParameters(
                                        parameterWithName("postId").description("게시물 아이디")
                                ),
                                responseFields(
                                        fieldWithPath("isSuccess").type(BOOLEAN).description("성공 여부"),
                                        fieldWithPath("code").type(STRING).description("결과 코드"),
                                        fieldWithPath("message").type(STRING).description("결과 메세지")
                                )
                        )
                );
    }

    @Test
    @DisplayName("[게시물 업로드] 중복 회원 태그시 유효성 검사를 한다.")
    void uploadPost_WhenTagDuplicateMember() throws Exception {
        String caption = "test caption";
        final List<String> taggedMemberHandles = List.of(
                MEMBER1.getHandle(),
                MEMBER1.getHandle()
        );

        this.mockMvc.perform(
                multipart("/api/v2/posts")
                        .file(getMockMultipartFileOfPost())
                        .queryParam("taggedMemberHandleList", String.join(", ", taggedMemberHandles))
                        .queryParam("caption", caption)
                        .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
        ).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[게시물 업로드] 최대 태그 가능한 회원 도달시 유효성 검사를 한다.")
    void uploadPost_WhenTagMaxMember() throws Exception {
        String caption = "test caption";
        final List<String> taggedMemberHandles = List.of(
                "member1",
                "member2",
                "member3",
                "member4",
                "member5",
                "member6"
        );

        this.mockMvc.perform(
                multipart("/api/v2/posts")
                        .file(getMockMultipartFileOfPost())
                        .queryParam("taggedMemberHandleList", String.join(", ", taggedMemberHandles))
                        .queryParam("caption", caption)
                        .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
        ).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[게시물 업로드] 태그 가능한 회원에 도달하지 않았을 시 유효성 검사를 한다.")
    void uploadPost_WhenTagMinMember() throws Exception {
        String caption = "test caption";
        final List<String> taggedMemberHandles = List.of();

        this.mockMvc.perform(
                multipart("/api/v2/posts")
                        .file(getMockMultipartFileOfPost())
                        .queryParam("taggedMemberHandleList", String.join(", ", taggedMemberHandles))
                        .queryParam("caption", caption)
                        .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
        ).andExpect(status().isBadRequest());
    }
}