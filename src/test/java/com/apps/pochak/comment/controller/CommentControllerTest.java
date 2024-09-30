package com.apps.pochak.comment.controller;

import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.comment.domain.Comment;
import com.apps.pochak.comment.dto.request.CommentUploadRequest;
import com.apps.pochak.comment.dto.response.CommentElements;
import com.apps.pochak.comment.dto.response.ParentCommentElement;
import com.apps.pochak.comment.service.CommentService;
import com.apps.pochak.global.ControllerTest;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;

import java.util.List;

import static com.apps.pochak.comment.fixture.CommentFixture.STATIC_PARENT_COMMENT;
import static com.apps.pochak.global.ApiDocumentUtils.getDocumentRequest;
import static com.apps.pochak.global.ApiDocumentUtils.getDocumentResponse;
import static com.apps.pochak.global.converter.ListToPageConverter.toPage;
import static com.apps.pochak.member.fixture.MemberFixture.STATIC_MEMBER1;
import static com.apps.pochak.post.fixture.PostFixture.STATIC_PUBLIC_POST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureRestDocs
@WebMvcTest(CommentController.class)
@MockBean(JpaMetamodelMappingContext.class)
class CommentControllerTest extends ControllerTest {

    private static final Member MEMBER1 = STATIC_MEMBER1;
    private static final Post POST = STATIC_PUBLIC_POST;
    private static final Comment PARENT_COMMENT = STATIC_PARENT_COMMENT;

    private static final List<Comment> PARENT_COMMENT_LIST = List.of(
            STATIC_PARENT_COMMENT
    );

    @MockBean
    CommentService commentService;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        given(jwtProvider.validateAccessToken(any())).willReturn(true);
        given(jwtProvider.getSubject(any())).willReturn(MEMBER1.getId().toString());
        given(loginArgumentResolver.resolveArgument(any(), any(), any(), any()))
                .willReturn(Accessor.member(MEMBER1.getId()));
    }

    @Test
    @DisplayName("게시물의 댓글창을 조회한다.")
    void getComments() throws Exception {

        when(commentService.getComments(any(), any(), any()))
                .thenReturn(new CommentElements(MEMBER1, toPage(PARENT_COMMENT_LIST)));

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v2/posts/{postId}/comments", POST.getId())
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("get-comments",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization")
                                                .description(
                                                        "Basic auth credentials " +
                                                                ": 만약 아직 공개된 게시물이 아니라면 (댓글이 당연히 없으므로) 오류가 발생합니다."
                                                )
                                ),
                                pathParameters(
                                        parameterWithName("postId").description("게시물 아이디")
                                ),
                                queryParameters(
                                        parameterWithName("page").description("조회할 페이지 [default: 0]").optional()
                                ),
                                responseFields(
                                        fieldWithPath("isSuccess").type(BOOLEAN).description("성공 여부"),
                                        fieldWithPath("code").type(STRING).description("결과 코드"),
                                        fieldWithPath("message").type(STRING).description("결과 메세지"),
                                        fieldWithPath("result").type(OBJECT).description("결과 데이터"),
                                        fieldWithPath("result.parentCommentPageInfo").type(OBJECT).description("부모 댓글 페이징 정보"),
                                        fieldWithPath("result.parentCommentPageInfo.lastPage").type(BOOLEAN)
                                                .description(
                                                        "부모 댓글 페이징 정보: 현재 페이지가 마지막 페이지인지의 여부"
                                                ),
                                        fieldWithPath("result.parentCommentPageInfo.totalPages").type(NUMBER)
                                                .description(
                                                        "부모 댓글 페이징 정보: 총 페이지 수"
                                                ),
                                        fieldWithPath("result.parentCommentPageInfo.totalElements").type(NUMBER)
                                                .description(
                                                        "부모 댓글 페이징 정보: 총 부모 댓글의 수"
                                                ),
                                        fieldWithPath("result.parentCommentPageInfo.size").type(NUMBER)
                                                .description(
                                                        "부모 댓글 페이징 정보: 페이징 사이즈"
                                                ),
                                        fieldWithPath("result.parentCommentList").type(ARRAY).description("부모 댓글 리스트"),
                                        fieldWithPath("result.parentCommentList[].commentId").type(NUMBER)
                                                .description(
                                                        "부모 댓글 리스트 | 댓글 아이디"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].memberId").type(NUMBER)
                                                .description(
                                                        "부모 댓글 리스트 | 작성자 아이디"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].profileImage").type(STRING)
                                                .description(
                                                        "부모 댓글 리스트 | 작성자 프로필 사진"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].handle").type(STRING)
                                                .description(
                                                        "부모 댓글 리스트 | 작성자 핸들 (handle)"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].createdDate").type(STRING)
                                                .description(
                                                        "부모 댓글 리스트 | 댓글 작성 시간"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].content").type(STRING)
                                                .description(
                                                        "부모 댓글 리스트 | 댓글 내용"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].childCommentPageInfo").type(OBJECT)
                                                .description(
                                                        "부모 댓글 리스트 | 자식 댓글 페이징 정보"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].childCommentPageInfo.lastPage").type(BOOLEAN)
                                                .description(
                                                        "부모 댓글 리스트 | 자식 댓글 페이징 정보 | 현재 페이지가 마지막 페이지인지의 여부"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].childCommentPageInfo.totalPages").type(NUMBER)
                                                .description(
                                                        "부모 댓글 리스트 | 자식 댓글 페이징 정보 | 총 페이지 수"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].childCommentPageInfo.totalElements").type(NUMBER)
                                                .description(
                                                        "부모 댓글 리스트 | 자식 댓글 페이징 정보 | 총 자식 댓글의 수"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].childCommentPageInfo.size").type(NUMBER)
                                                .description(
                                                        "부모 댓글 리스트 | 자식 댓글 페이징 정보 | 페이징 사이즈"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].childCommentList").type(ARRAY)
                                                .description(
                                                        "부모 댓글 리스트 | 자식 댓글 리스트"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].childCommentList[].commentId").type(NUMBER)
                                                .description(
                                                        "부모 댓글 리스트 | 자식 댓글 리스트 | 자식 댓글 아이디"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].childCommentList[].memberId").type(NUMBER)
                                                .description(
                                                        "부모 댓글 리스트 | 자식 댓글 리스트 | 작성자 아이디"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].childCommentList[].profileImage").type(STRING)
                                                .description(
                                                        "부모 댓글 리스트 | 자식 댓글 리스트 | 작성자 프로필 이미지"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].childCommentList[].handle").type(STRING)
                                                .description(
                                                        "부모 댓글 리스트 | 자식 댓글 리스트 | 작성자 핸들"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].childCommentList[].createdDate").type(STRING)
                                                .description(
                                                        "부모 댓글 리스트 | 자식 댓글 리스트 | 댓글 작성 시간"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].childCommentList[].content").type(STRING)
                                                .description(
                                                        "부모 댓글 리스트 | 자식 댓글 리스트 | 댓글 내용"
                                                ).optional(),
                                        fieldWithPath("result.loginMemberProfileImage").type(STRING)
                                                .description(
                                                        "로그인한 멤버의 프로필 이미지"
                                                )
                                )

                        )
                );
    }

    @Test
    @DisplayName("부모 댓글의 자식 댓글들을 조회한다.")
    void getChildComments() throws Exception {

        when(commentService.getChildCommentsByParentCommentId(any(), any(), any(), any()))
                .thenReturn(new ParentCommentElement(PARENT_COMMENT));

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v2/posts/{postId}/comments/{commentId}", POST.getId(), PARENT_COMMENT.getId())
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("get-child-comments",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization")
                                                .description(
                                                        "Basic auth credentials " +
                                                                ": 만약 아직 공개된 게시물이 아니라면 (댓글이 당연히 없으므로) 오류가 발생합니다."
                                                )
                                ),
                                pathParameters(
                                        parameterWithName("postId").description("게시물 아이디"),
                                        parameterWithName("commentId").description("부모 댓글 아이디")
                                ),
                                queryParameters(
                                        parameterWithName("page").description("조회할 페이지 [default: 0]").optional()
                                ),
                                responseFields(
                                        fieldWithPath("isSuccess").type(BOOLEAN).description("성공 여부"),
                                        fieldWithPath("code").type(STRING).description("결과 코드"),
                                        fieldWithPath("message").type(STRING).description("결과 메세지"),
                                        fieldWithPath("result").type(OBJECT).description("결과 데이터"),
                                        fieldWithPath("result.commentId").type(NUMBER)
                                                .description(
                                                        "부모 댓글 아이디"
                                                ).optional(),
                                        fieldWithPath("result.memberId").type(NUMBER)
                                                .description(
                                                        "부모 댓글 작성자 아이디"
                                                ).optional(),
                                        fieldWithPath("result.profileImage").type(STRING)
                                                .description(
                                                        "부모 댓글 작성자 프로필 사진"
                                                ).optional(),
                                        fieldWithPath("result.handle").type(STRING)
                                                .description(
                                                        "부모 댓글 작성자 핸들"
                                                ).optional(),
                                        fieldWithPath("result.createdDate").type(STRING)
                                                .description(
                                                        "부모 댓글 작성 시간"
                                                ).optional(),
                                        fieldWithPath("result.content").type(STRING)
                                                .description(
                                                        "부모 댓글 내용"
                                                ).optional(),
                                        fieldWithPath("result.childCommentPageInfo").type(OBJECT)
                                                .description(
                                                        "자식 댓글 페이징 정보"
                                                ).optional(),
                                        fieldWithPath("result.childCommentPageInfo.lastPage").type(BOOLEAN)
                                                .description(
                                                        "자식 댓글 페이징 정보 | 현재 페이지가 마지막 페이지인지의 여부"
                                                ).optional(),
                                        fieldWithPath("result.childCommentPageInfo.totalPages").type(NUMBER)
                                                .description(
                                                        "자식 댓글 페이징 정보 | 총 페이지 수"
                                                ).optional(),
                                        fieldWithPath("result.childCommentPageInfo.totalElements").type(NUMBER)
                                                .description(
                                                        "자식 댓글 페이징 정보 | 총 자식 댓글의 수"
                                                ).optional(),
                                        fieldWithPath("result.childCommentPageInfo.size").type(NUMBER)
                                                .description(
                                                        "자식 댓글 페이징 정보 | 페이징 사이즈"
                                                ).optional(),
                                        fieldWithPath("result.childCommentList").type(ARRAY)
                                                .description(
                                                        "자식 댓글 리스트"
                                                ).optional(),
                                        fieldWithPath("result.childCommentList[].commentId").type(NUMBER)
                                                .description(
                                                        "자식 댓글 리스트 | 자식 댓글 아이디"
                                                ).optional(),
                                        fieldWithPath("result.childCommentList[].memberId").type(NUMBER)
                                                .description(
                                                        "자식 댓글 리스트 | 작성자 아이디"
                                                ).optional(),
                                        fieldWithPath("result.childCommentList[].profileImage").type(STRING)
                                                .description(
                                                        "자식 댓글 리스트 | 작성자 프로필 사진"
                                                ).optional(),
                                        fieldWithPath("result.childCommentList[].handle").type(STRING)
                                                .description(
                                                        "자식 댓글 리스트 | 작성자 핸들"
                                                ).optional(),
                                        fieldWithPath("result.childCommentList[].createdDate").type(STRING)
                                                .description(
                                                        "자식 댓글 리스트 | 댓글 작성 시간"
                                                ).optional(),
                                        fieldWithPath("result.childCommentList[].content").type(STRING)
                                                .description(
                                                        "자식 댓글 리스트 | 댓글 내용"
                                                ).optional()
                                )

                        )
                );
    }

    @Test
    @DisplayName("댓글을 작성하고, 저장한다.")
    void uploadCommentTest() throws Exception {

        doNothing().when(commentService).saveComment(any(), any(), any());

        final CommentUploadRequest uploadRequest = new CommentUploadRequest(
                "댓글 업로드 테스트",
                null
        );

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .post("/api/v2/posts/{postId}/comments", POST.getId())
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
                                .content(objectMapper.writeValueAsString(uploadRequest))
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("upload-comment",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization")
                                                .description(
                                                        "Basic auth credentials  " +
                                                                ": 만약 아직 공개된 게시물이 아니라면 오류가 발생합니다."
                                                )
                                ),
                                pathParameters(
                                        parameterWithName("postId").description("게시물 아이디")
                                ),
                                requestFields(
                                        fieldWithPath("content").type(STRING).description("댓글 내용"),
                                        fieldWithPath("parentCommentId").type(NUMBER)
                                                .description(
                                                        "부모 댓글 아이디: " +
                                                                "만약 자식 댓글을 작성하고 싶은 경우 부모 댓글 아이디를 전달하고, " +
                                                                "부모 댓글을 작성하고 싶은 경우 null값으로 전달합니다."
                                                ).optional()
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
    @DisplayName("댓글을 삭제한다.")
    void deleteCommentTest() throws Exception {

        doNothing().when(commentService).deleteComment(any(), any(), any());

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .delete("/api/v2/posts/{postId}/comments", POST.getId())
                                .queryParam("commentId", PARENT_COMMENT.getId().toString())
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("delete-comment",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization").description("Basic auth credentials : 댓글은 게시글의 주인(찍은 사람 + 찍힌 사람들)과 댓글을 작성한 사람들만 삭제할 수 있습니다.")
                                ),
                                pathParameters(
                                        parameterWithName("postId").description("게시물 아이디")
                                ),
                                queryParameters(
                                        parameterWithName("commentId").description("삭제하려는 댓글 아이디").optional()
                                ),
                                responseFields(
                                        fieldWithPath("isSuccess").type(BOOLEAN).description("성공 여부"),
                                        fieldWithPath("code").type(STRING).description("결과 코드"),
                                        fieldWithPath("message").type(STRING).description("결과 메세지")
                                )

                        )
                );
    }
}