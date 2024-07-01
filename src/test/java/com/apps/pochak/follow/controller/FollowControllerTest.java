package com.apps.pochak.follow.controller;

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
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
class FollowControllerTest {
    @Value("${test.authorization.master1}")
    String authorization1;

    @Value("${test.authorization.master2}")
    String authorization2;

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
    @DisplayName("get followings API Document")
    void getFollowings() throws Exception {

        String handle = "_skf__11";

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v2/members/{handle}/following", handle)
                                .header("Authorization", authorization2)
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
    @DisplayName("get follower API Document")
    void getFollowers() throws Exception {

        String handle = "dxxynni";

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v2/members/{handle}/follower", handle)
                                .header("Authorization", authorization2)
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
    @Transactional
    @DisplayName("follow member API Document")
    void followTest() throws Exception {

        String handle = "master1";

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .post("/api/v2/members/{handle}/follow", handle)
                                .header("Authorization", authorization2)
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
    @Transactional
    @DisplayName("delete follower API Document")
    void deleteFollowerTest() throws Exception {

        String handle = "master1";
        String followerHandle = "master2";

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .delete("/api/v2/members/{handle}/follower", handle)
                                .queryParam("followerHandle", followerHandle)
                                .header("Authorization", authorization1)
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