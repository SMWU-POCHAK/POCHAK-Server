package com.apps.pochak.like.controller;

import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.global.ControllerTest;
import com.apps.pochak.like.domain.LikeEntity;
import com.apps.pochak.like.dto.response.LikeElement;
import com.apps.pochak.like.dto.response.LikeElements;
import com.apps.pochak.like.service.LikeService;
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

import static com.apps.pochak.global.ApiDocumentUtils.getDocumentRequest;
import static com.apps.pochak.global.ApiDocumentUtils.getDocumentResponse;
import static com.apps.pochak.like.fixture.LikeFixture.LIKE1;
import static com.apps.pochak.like.fixture.LikeFixture.LIKE2;
import static com.apps.pochak.member.fixture.MemberFixture.MEMBER1;
import static com.apps.pochak.member.fixture.MemberFixture.MEMBER2;
import static com.apps.pochak.post.fixture.PostFixture.PUBLIC_POST;
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
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureRestDocs
@WebMvcTest(LikeController.class)
@MockBean(JpaMetamodelMappingContext.class)
public class LikeControllerTest extends ControllerTest {

    private static final List<LikeEntity> LIKE_LIST = List.of(
            LIKE1,
            LIKE2
    );

    @MockBean
    LikeService likeService;

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
    @DisplayName("게시물에 좋아요를 누른다.")
    void likePost() throws Exception {

        doNothing().when(likeService).likePost(any(), any());

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .post("/api/v2/posts/{postId}/like", PUBLIC_POST.getId())
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("like-post",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization")
                                                .description("Basic auth credentials")
                                ),
                                pathParameters(
                                        parameterWithName("postId").description("조회하고자 하는 포스트 아이디(postId)")
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
    @DisplayName("게시물에 좋아요를 누른 멤버 목록을 조회한다.")
    void getMemberLikedPost() throws Exception {

        List<LikeElement> likeElementList = List.of(
                new LikeElement(MEMBER1, true),
                new LikeElement(MEMBER2, true)
        );

        when(likeService.getMemberLikedPost(any(), any()))
                .thenReturn(new LikeElements(likeElementList));

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v2/posts/{postId}/like", PUBLIC_POST.getId())
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("get-like",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization")
                                                .description("Basic auth credentials")
                                ),
                                pathParameters(
                                        parameterWithName("postId").description("조회하고자 하는 포스트 아이디(postId)")
                                ),
                                responseFields(
                                        fieldWithPath("isSuccess").type(BOOLEAN).description("성공 여부"),
                                        fieldWithPath("code").type(STRING).description("결과 코드"),
                                        fieldWithPath("message").type(STRING).description("결과 메세지"),
                                        fieldWithPath("result").type(OBJECT).description("결과 데이터"),
                                        fieldWithPath("result.likeMembersList").description("좋아요를 누른 사람들의 리스트").type(ARRAY),
                                        fieldWithPath("result.likeMembersList[].memberId").type(NUMBER)
                                                .description("유저 아이디").optional(),
                                        fieldWithPath("result.likeMembersList[].handle").type(STRING)
                                                .description("유저 핸들").optional(),
                                        fieldWithPath("result.likeMembersList[].profileImage").type(STRING)
                                                .description("프로필 이미지 url").optional(),
                                        fieldWithPath("result.likeMembersList[].name").type(STRING)
                                                .description("유저 이름").optional(),
                                        fieldWithPath("result.likeMembersList[].follow").type(BOOLEAN)
                                                .description("팔로우 여부").optional()
                                )
                        )
                );
    }
}
