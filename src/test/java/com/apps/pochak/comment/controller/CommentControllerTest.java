package com.apps.pochak.comment.controller;

import com.apps.pochak.comment.dto.request.CommentUploadRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static com.apps.pochak.common.ApiDocumentUtils.getDocumentRequest;
import static com.apps.pochak.common.ApiDocumentUtils.getDocumentResponse;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
class CommentControllerTest {
    @Value("${test.authorization.master1}")
    String authorization;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    WebApplicationContext wac;

    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    @DisplayName("Get Comments API Document")
    void getComments() throws Exception {
        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v2/posts/{postId}/comments", 2)
                                .header("Authorization", authorization)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("get-comments",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization")
                                                .description(
                                                        "Basic auth credentials  \n" +
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
                                                        "부모 댓글 페이징 정보 \n" +
                                                                ": 현재 페이지가 마지막 페이지인지의 여부"
                                                ),
                                        fieldWithPath("result.parentCommentPageInfo.totalPages").type(NUMBER)
                                                .description(
                                                        "부모 댓글 페이징 정보 \n" +
                                                                ": 총 페이지 수"
                                                ),
                                        fieldWithPath("result.parentCommentPageInfo.totalElements").type(NUMBER)
                                                .description(
                                                        "부모 댓글 페이징 정보 \n" +
                                                                ": 총 부모 댓글의 수"
                                                ),
                                        fieldWithPath("result.parentCommentPageInfo.size").type(NUMBER)
                                                .description(
                                                        "부모 댓글 페이징 정보 \n" +
                                                                ": 페이징 사이즈"
                                                ),
                                        fieldWithPath("result.parentCommentList").type(ARRAY).description("부모 댓글 리스트"),
                                        fieldWithPath("result.parentCommentList[].commentId").type(NUMBER)
                                                .description(
                                                        "부모 댓글 리스트 \n" +
                                                                ": 댓글 아이디"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].profileImage").type(STRING)
                                                .description(
                                                        "부모 댓글 리스트 \n" +
                                                                ": 작성자 프로필 사진"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].handle").type(STRING)
                                                .description(
                                                        "부모 댓글 리스트 \n" +
                                                                ": 작성자 아이디"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].createdDate").type(STRING)
                                                .description(
                                                        "부모 댓글 리스트 \n" +
                                                                ": 댓글 작성 시간"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].content").type(STRING)
                                                .description(
                                                        "부모 댓글 리스트 \n" +
                                                                ": 댓글 내용"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].childCommentPageInfo").type(OBJECT)
                                                .description(
                                                        "부모 댓글 리스트 \n" +
                                                                ": 자식 댓글 페이징 정보"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].childCommentPageInfo.lastPage").type(BOOLEAN)
                                                .description(
                                                        "부모 댓글 리스트 \n" +
                                                                ": 자식 댓글 페이징 정보 \n" +
                                                                ": 현재 페이지가 마지막 페이지인지의 여부"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].childCommentPageInfo.totalPages").type(NUMBER)
                                                .description(
                                                        "부모 댓글 리스트 \n" +
                                                                ": 자식 댓글 페이징 정보 \n" +
                                                                ": 총 페이지 수"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].childCommentPageInfo.totalElements").type(NUMBER)
                                                .description(
                                                        "부모 댓글 리스트 \n" +
                                                                ": 자식 댓글 페이징 정보 \n" +
                                                                ": 총 자식 댓글의 수"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].childCommentPageInfo.size").type(NUMBER)
                                                .description(
                                                        "부모 댓글 리스트 \n" +
                                                                ": 자식 댓글 페이징 정보 \n" +
                                                                ": 페이징 사이즈"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].childCommentList").type(ARRAY)
                                                .description(
                                                        "부모 댓글 리스트 \n" +
                                                                ": 자식 댓글 리스트"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].childCommentList[].commentId").type(NUMBER)
                                                .description(
                                                        "부모 댓글 리스트 \n" +
                                                                ": 자식 댓글 리스트 \n" +
                                                                ": 자식 댓글 아이디"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].childCommentList[].profileImage").type(STRING)
                                                .description(
                                                        "부모 댓글 리스트 \n" +
                                                                ": 자식 댓글 리스트 \n" +
                                                                ": 작성자 프로필 사진"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].childCommentList[].handle").type(STRING)
                                                .description(
                                                        "부모 댓글 리스트 \n" +
                                                                ": 자식 댓글 리스트 \n" +
                                                                ": 작성자 아이디"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].childCommentList[].createdDate").type(STRING)
                                                .description(
                                                        "부모 댓글 리스트 \n" +
                                                                ": 자식 댓글 리스트 \n" +
                                                                ": 댓글 작성 시간"
                                                ).optional(),
                                        fieldWithPath("result.parentCommentList[].childCommentList[].content").type(STRING)
                                                .description(
                                                        "부모 댓글 리스트 \n" +
                                                                ": 자식 댓글 리스트 \n" +
                                                                ": 댓글 내용"
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
    @DisplayName("Get Child Comments API Document")
    void getChildComments() throws Exception {
        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v2/posts/{postId}/comments/{commentId}", 2, 3)
                                .header("Authorization", authorization)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("get-child-comments",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization")
                                                .description(
                                                        "Basic auth credentials  \n" +
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
                                        fieldWithPath("result.profileImage").type(STRING)
                                                .description(
                                                        "부모 댓글 작성자 프로필 사진"
                                                ).optional(),
                                        fieldWithPath("result.handle").type(STRING)
                                                .description(
                                                        "부모 댓글 작성자 아이디"
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
                                                        "자식 댓글 페이징 정보 \n" +
                                                                ": 현재 페이지가 마지막 페이지인지의 여부"
                                                ).optional(),
                                        fieldWithPath("result.childCommentPageInfo.totalPages").type(NUMBER)
                                                .description(
                                                        "자식 댓글 페이징 정보 \n" +
                                                                ": 총 페이지 수"
                                                ).optional(),
                                        fieldWithPath("result.childCommentPageInfo.totalElements").type(NUMBER)
                                                .description(
                                                        "자식 댓글 페이징 정보 \n" +
                                                                ": 총 자식 댓글의 수"
                                                ).optional(),
                                        fieldWithPath("result.childCommentPageInfo.size").type(NUMBER)
                                                .description(
                                                        "자식 댓글 페이징 정보 \n" +
                                                                ": 페이징 사이즈"
                                                ).optional(),
                                        fieldWithPath("result.childCommentList").type(ARRAY)
                                                .description(
                                                        "자식 댓글 리스트"
                                                ).optional(),
                                        fieldWithPath("result.childCommentList[].commentId").type(NUMBER)
                                                .description(
                                                        "자식 댓글 리스트 \n" +
                                                                ": 자식 댓글 아이디"
                                                ).optional(),
                                        fieldWithPath("result.childCommentList[].profileImage").type(STRING)
                                                .description(
                                                        "자식 댓글 리스트 \n" +
                                                                ": 작성자 프로필 사진"
                                                ).optional(),
                                        fieldWithPath("result.childCommentList[].handle").type(STRING)
                                                .description(
                                                        "자식 댓글 리스트 \n" +
                                                                ": 작성자 아이디"
                                                ).optional(),
                                        fieldWithPath("result.childCommentList[].createdDate").type(STRING)
                                                .description(
                                                        "자식 댓글 리스트 \n" +
                                                                ": 댓글 작성 시간"
                                                ).optional(),
                                        fieldWithPath("result.childCommentList[].content").type(STRING)
                                                .description(
                                                        "자식 댓글 리스트 \n" +
                                                                ": 댓글 내용"
                                                ).optional()
                                )

                        )
                );
    }

    @Test
    @Transactional
    @DisplayName("Comment Upload API Document")
    void uploadCommentTest() throws Exception {

        final CommentUploadRequest uploadRequest = new CommentUploadRequest("댓글 내용 테스트", 13L);

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .post("/api/v2/posts/{postId}/comments", 2)
                                .header("Authorization", authorization)
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
                                                        "Basic auth credentials  \n" +
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
                                                        "부모 댓글 아이디: \n" +
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
    @Transactional
    @DisplayName("Delete Comment API Document")
    void deleteCommentTest() throws Exception {

        Long postId = 2L;
        String commentId = "3";

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .delete("/api/v2/posts/{postId}/comments", postId)
                                .queryParam("commentId", commentId)
                                .header("Authorization", authorization)
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