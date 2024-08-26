package com.apps.pochak.follow.controller;

import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.follow.service.FollowService;
import com.apps.pochak.global.ControllerTest;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.dto.response.MemberElements;
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
import static com.apps.pochak.global.api_payload.code.status.SuccessStatus.SUCCESS_FOLLOW;
import static com.apps.pochak.global.converter.ListToPageConverter.toPage;
import static com.apps.pochak.member.fixture.MemberFixture.MEMBER1;
import static com.apps.pochak.member.fixture.MemberFixture.MEMBER2;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureRestDocs
@WebMvcTest(FollowController.class)
@MockBean(JpaMetamodelMappingContext.class)
class FollowControllerTest extends ControllerTest {

    private static final List<Member> MEMBER_LIST = List.of(
            MEMBER1,
            MEMBER2
    );

    @MockBean
    FollowService followService;

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
    @DisplayName("프로필에서 팔로잉 목록을 조회한다.")
    void getFollowings() throws Exception {

        when(followService.getFollowings(any(), any(), any()))
                .thenReturn(MemberElements.from(toPage(MEMBER_LIST)));

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v2/members/{handle}/following", MEMBER1.getHandle())
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("get-followings",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization").description("Basic auth credentials")
                                ),
                                pathParameters(
                                        parameterWithName("handle").description("조회하고자 하는 멤버의 아이디(handle)")
                                ),
                                queryParameters(
                                        parameterWithName("page").description("조회할 페이지 [default: 0]").optional()
                                ),
                                responseFields(
                                        fieldWithPath("isSuccess").type(BOOLEAN).description("성공 여부"),
                                        fieldWithPath("code").type(STRING).description("결과 코드"),
                                        fieldWithPath("message").type(STRING).description("결과 메세지"),
                                        fieldWithPath("result").type(OBJECT).description("결과 데이터"),
                                        fieldWithPath("result.pageInfo").type(OBJECT).description("팔로잉 페이징 정보"),
                                        fieldWithPath("result.pageInfo.lastPage").type(BOOLEAN)
                                                .description(
                                                        "팔로잉 페이징 정보: 현재 페이지가 마지막 페이지인지의 여부"
                                                ),
                                        fieldWithPath("result.pageInfo.totalPages").type(NUMBER)
                                                .description(
                                                        "팔로잉 페이징 정보: 총 페이지 수"
                                                ),
                                        fieldWithPath("result.pageInfo.totalElements").type(NUMBER)
                                                .description(
                                                        "팔로잉 페이징 정보: 총 팔로잉 수"
                                                ),
                                        fieldWithPath("result.pageInfo.size").type(NUMBER)
                                                .description(
                                                        "팔로잉 페이징 정보: 페이징 사이즈"
                                                ),
                                        fieldWithPath("result.memberList").type(ARRAY).description("팔로잉 리스트"),
                                        fieldWithPath("result.memberList[].memberId").type(NUMBER)
                                                .description("팔로워 리스트: 멤버 아이디").optional(),
                                        fieldWithPath("result.memberList[].profileImage").type(STRING)
                                                .description("팔로잉 리스트: 프로필 이미지").optional(),
                                        fieldWithPath("result.memberList[].handle").type(STRING)
                                                .description("팔로잉 리스트: 멤버 핸들").optional(),
                                        fieldWithPath("result.memberList[].name").type(STRING)
                                                .description("팔로잉 리스트: 멤버 이름").optional(),
                                        fieldWithPath("result.memberList[].isFollow").type(BOOLEAN)
                                                .description(
                                                        "팔로잉 리스트: 현재 로그인한 멤버가 해당 팔로잉 멤버를 팔로우하고 있는지의 여부, 만약 자신이라면 null이 반환됨"
                                                ).optional()
                                )
                        )
                );
    }

    @Test
    @DisplayName("프로필에서 팔로워 목록을 조회한다.")
    void getFollowers() throws Exception {

        when(followService.getFollowers(any(), any(), any()))
                .thenReturn(MemberElements.from(toPage(MEMBER_LIST)));

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v2/members/{handle}/follower", MEMBER1.getHandle())
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("get-followers",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization").description("Basic auth credentials")
                                ),
                                pathParameters(
                                        parameterWithName("handle").description("조회하고자 하는 멤버의 아이디(handle)")
                                ),
                                queryParameters(
                                        parameterWithName("page").description("조회할 페이지 [default: 0]").optional()
                                ),
                                responseFields(
                                        fieldWithPath("isSuccess").type(BOOLEAN).description("성공 여부"),
                                        fieldWithPath("code").type(STRING).description("결과 코드"),
                                        fieldWithPath("message").type(STRING).description("결과 메세지"),
                                        fieldWithPath("result").type(OBJECT).description("결과 데이터"),
                                        fieldWithPath("result.pageInfo").type(OBJECT).description("팔로워 페이징 정보"),
                                        fieldWithPath("result.pageInfo.lastPage").type(BOOLEAN)
                                                .description(
                                                        "팔로워 페이징 정보: 현재 페이지가 마지막 페이지인지의 여부"
                                                ),
                                        fieldWithPath("result.pageInfo.totalPages").type(NUMBER)
                                                .description(
                                                        "팔로워 페이징 정보: 총 페이지 수"
                                                ),
                                        fieldWithPath("result.pageInfo.totalElements").type(NUMBER)
                                                .description(
                                                        "팔로워 페이징 정보: 총 팔로워 수"
                                                ),
                                        fieldWithPath("result.pageInfo.size").type(NUMBER)
                                                .description(
                                                        "팔로워 페이징 정보: 페이징 사이즈"
                                                ),
                                        fieldWithPath("result.memberList").type(ARRAY).description("팔로워 리스트"),
                                        fieldWithPath("result.memberList[].memberId").type(NUMBER)
                                                .description("팔로워 리스트: 멤버 아이디").optional(),
                                        fieldWithPath("result.memberList[].profileImage").type(STRING)
                                                .description("팔로워 리스트: 프로필 이미지").optional(),
                                        fieldWithPath("result.memberList[].handle").type(STRING)
                                                .description("팔로워 리스트: 멤버 핸들").optional(),
                                        fieldWithPath("result.memberList[].name").type(STRING)
                                                .description("팔로워 리스트: 멤버 이름").optional(),
                                        fieldWithPath("result.memberList[].isFollow").type(BOOLEAN)
                                                .description(
                                                        "팔로잉 리스트: 현재 로그인한 멤버가 해당 팔로워를 팔로우하고 있는지의 여부, 만약 자신이라면 null이 반환됨"
                                                ).optional()
                                )
                        )
                );
    }

    @Test
    @DisplayName("다른 멤버를 팔로우한다.")
    void followTest() throws Exception {

        when(followService.follow(any(), any()))
                .thenReturn(SUCCESS_FOLLOW);

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .post("/api/v2/members/{handle}/follow", MEMBER2.getHandle())
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("follow",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization").description("Basic auth credentials")
                                ),
                                pathParameters(
                                        parameterWithName("handle").description("팔로우하고자 하는 멤버의 아이디(handle): 만약 로그인한 아이디와 동일하다면 404 오류 발생")
                                ),
                                queryParameters(
                                        parameterWithName("page").description("조회할 페이지 [default: 0]").optional()
                                ),
                                responseFields(
                                        fieldWithPath("isSuccess").type(BOOLEAN).description("성공 여부"),
                                        fieldWithPath("code").type(STRING).description("결과 코드"),
                                        fieldWithPath("message").type(STRING)
                                                .description("결과 메세지: 팔로우하였을 경우 `성공적으로 팔로우하였습니다`, 팔로우를 취소하였을 경우 `성공적으로 팔로우를 취소하였습니다.`를 반환함.")
                                )
                        )
                );
    }

    @Test
    @DisplayName("프로필에서 팔로워를 삭제한다.")
    void deleteFollowerTest() throws Exception {

        doNothing().when(followService).deleteFollower(any(), any(), any());

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .delete("/api/v2/members/{handle}/follower", MEMBER1.getHandle())
                                .queryParam("followerHandle", MEMBER2.getHandle())
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("delete-follower",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization").description("Basic auth credentials: path variable에서 전달된 아이디와 다른 멤버로 로그인할 경우 팔로워 삭제 권한 에러가 발생합니다.")
                                ),
                                pathParameters(
                                        parameterWithName("handle").description("멤버 아이디: 팔로워를 삭제하고자 하는 멤버 (로그인 헤더 정보와 동일해야 함)")
                                ),
                                queryParameters(
                                        parameterWithName("followerHandle").description("삭제하려는 팔로워 아이디").optional()
                                ),
                                responseFields(
                                        fieldWithPath("isSuccess").type(BOOLEAN).description("성공 여부"),
                                        fieldWithPath("code").type(STRING).description("결과 코드"),
                                        fieldWithPath("message").type(STRING)
                                                .description("결과 메세지: `성공적으로 팔로워를 삭제하였습니다.`")
                                )
                        )
                );
    }

}