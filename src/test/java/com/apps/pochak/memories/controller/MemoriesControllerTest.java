package com.apps.pochak.memories.controller;

import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.global.ControllerTest;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.memories.dto.response.MemoriesPostResponse;
import com.apps.pochak.memories.dto.response.MemoriesPreviewResponse;
import com.apps.pochak.memories.service.MemoriesService;
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

import static com.apps.pochak.follow.fixture.FollowFixture.STATIC_RECEIVE_FOLLOW;
import static com.apps.pochak.follow.fixture.FollowFixture.STATIC_SEND_FOLLOW;
import static com.apps.pochak.global.ApiDocumentUtils.getDocumentRequest;
import static com.apps.pochak.global.ApiDocumentUtils.getDocumentResponse;
import static com.apps.pochak.global.converter.ListToPageConverter.toPage;
import static com.apps.pochak.member.fixture.MemberFixture.STATIC_MEMBER1;
import static com.apps.pochak.member.fixture.MemberFixture.STATIC_MEMBER2;
import static com.apps.pochak.tag.fixture.TagFixture.STATIC_APPROVED_TAG;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureRestDocs
@WebMvcTest(MemoriesController.class)
@MockBean(JpaMetamodelMappingContext.class)
class MemoriesControllerTest extends ControllerTest {
    private static final Member MEMBER1 = STATIC_MEMBER1;
    private static final Member MEMBER2 = STATIC_MEMBER2;
    private static final Follow SEND_FOLLOW = STATIC_SEND_FOLLOW;
    private static final Follow RECEIVE_FOLLOW = STATIC_RECEIVE_FOLLOW;
    private static final Tag APPROVED_TAG = STATIC_APPROVED_TAG;

    @MockBean
    MemoriesService memoriesService;

    @BeforeEach
    void setUp() {
        given(jwtProvider.validateAccessToken(any())).willReturn(true);
        given(jwtProvider.getSubject(any())).willReturn(MEMBER1.getId().toString());
        given(loginArgumentResolver.resolveArgument(any(), any(), any(), any()))
                .willReturn(Accessor.member(MEMBER1.getId()));

        SEND_FOLLOW.updateLastModifiedDate();
        RECEIVE_FOLLOW.updateLastModifiedDate();
    }

    @Test
    @DisplayName("[추억 페이지] 프리뷰 페이지를 조회한다.")
    void getMemories() throws Exception {
        when(memoriesService.getMemories(any(), any()))
                .thenReturn(MemoriesPreviewResponse.of()
                        .loginMember(MEMBER1)
                        .member(MEMBER2)
                        .follow(SEND_FOLLOW)
                        .followed(RECEIVE_FOLLOW)
                        .countTag(20L)
                        .countTaggedWith(15L)
                        .countTagged(30L)
                        .firstTagged(APPROVED_TAG)
                        .firstTag(APPROVED_TAG)
                        .firstTaggedWith(APPROVED_TAG)
                        .latestTag(APPROVED_TAG)
                        .build()
                );

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v1/memories/{handle}", "member2")
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("get-memories",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization").description("Basic auth credentials")
                                ),
                                pathParameters(
                                        parameterWithName("handle").description("친구의 아이디")
                                ),
                                responseFields(
                                        fieldWithPath("isSuccess").type(BOOLEAN).description("성공 여부"),
                                        fieldWithPath("code").type(STRING).description("결과 코드"),
                                        fieldWithPath("message").type(STRING).description("결과 메세지"),
                                        fieldWithPath("result").type(OBJECT).description("결과 데이터"),

                                        fieldWithPath("result.handle").type(STRING).description("친구 아이디"),
                                        fieldWithPath("result.loginMemberProfileImage").type(STRING).description("내 프로필 이미지"),
                                        fieldWithPath("result.memberProfileImage").type(STRING).description("친구 프로필 이미지"),
                                        fieldWithPath("result.followDate").type(STRING).description("내가 친구를 팔로우한 날짜"),
                                        fieldWithPath("result.followedDate").type(STRING).description("친구가 나를 팔로우한 날짜"),
                                        fieldWithPath("result.followDay").type(NUMBER).description("맞팔로우한 지 몇일"),
                                        fieldWithPath("result.pochakCount").type(NUMBER).description("POCHAK 게시물 수"),
                                        fieldWithPath("result.bondedCount").type(NUMBER).description("BONDED 게시물 수"),
                                        fieldWithPath("result.pochakedCount").type(NUMBER).description("POCHAKED 게시물 수"),
                                        fieldWithPath("result.firstPochaked").type(OBJECT).description("처음 포착된 순간"),
                                        fieldWithPath("result.firstPochak").type(OBJECT).description("처음 포착한 순간"),
                                        fieldWithPath("result.firstBonded").type(OBJECT).description("처음 함께 포착된 순간"),
                                        fieldWithPath("result.latestPost").type(OBJECT).description("최근 포착 순간"),
                                        fieldWithPath("result.firstPochaked.postId").type(NUMBER).description("처음 포착된 순간의 게시물 아이디"),
                                        fieldWithPath("result.firstPochaked.postImage").type(STRING).description("처음 포착된 순간의 게시물 이미지"),
                                        fieldWithPath("result.firstPochaked.postDate").type(STRING).description("처음 포착된 순간의 게시물 날짜"),
                                        fieldWithPath("result.firstPochak.postId").type(NUMBER).description("처음 포착한 순간의 게시물 아이디"),
                                        fieldWithPath("result.firstPochak.postImage").type(STRING).description("처음 포착한 순간의 게시물 이미지"),
                                        fieldWithPath("result.firstPochak.postDate").type(STRING).description("처음 포착한 순간의 게시물 날짜"),
                                        fieldWithPath("result.firstBonded.postId").type(NUMBER).description("처음 함께 포착된 순간의 게시물 아이디"),
                                        fieldWithPath("result.firstBonded.postImage").type(STRING).description("처음 함께 포착된 순간의 게시물 이미지"),
                                        fieldWithPath("result.firstBonded.postDate").type(STRING).description("처음 함께 포착된 순간의 게시물 날짜"),
                                        fieldWithPath("result.latestPost.postId").type(NUMBER).description("최근 포착 순간의 게시물 아이디"),
                                        fieldWithPath("result.latestPost.postImage").type(STRING).description("최근 포착 순간의 게시물 이미지"),
                                        fieldWithPath("result.latestPost.postDate").type(STRING).description("최근 포착 순간의 게시물 날짜")
                                )
                        )
                );
    }

    @Test
    @DisplayName("[추억 페이지] pochak 페이지를 조회한다.")
    void getPochak() throws Exception {
        when(memoriesService.getPochak(any(), any(), any()))
                .thenReturn(MemoriesPostResponse.from(toPage(List.of(APPROVED_TAG))));

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v1/memories/{handle}/pochak", "member2")
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("get-memories-pochak",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization").description("Basic auth credentials")
                                ),
                                pathParameters(
                                        parameterWithName("handle").description("친구의 아이디")
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
    @DisplayName("[추억 페이지] pochaked 페이지를 조회한다.")
    void getPochaked() throws Exception {
        when(memoriesService.getPochaked(any(), any(), any()))
                .thenReturn(MemoriesPostResponse.from(toPage(List.of(APPROVED_TAG))));

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v1/memories/{handle}/pochaked", "member2")
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("get-memories-pochaked",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization").description("Basic auth credentials")
                                ),
                                pathParameters(
                                        parameterWithName("handle").description("친구의 아이디")
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
    @DisplayName("[추억 페이지] bonded 페이지를 조회한다.")
    void getBonded() throws Exception {
        when(memoriesService.getBonded(any(), any(), any()))
                .thenReturn(MemoriesPostResponse.from(toPage(List.of(APPROVED_TAG))));

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v1/memories/{handle}/bonded", "member2")
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("get-memories-bonded",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization").description("Basic auth credentials")
                                ),
                                pathParameters(
                                        parameterWithName("handle").description("친구의 아이디")
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
}