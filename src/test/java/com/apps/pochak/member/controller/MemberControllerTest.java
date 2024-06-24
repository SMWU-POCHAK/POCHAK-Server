package com.apps.pochak.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
class MemberControllerTest {
    @Value("${test.authorization.dayeon}")
    String authorization1;

    @Value("${test.authorization.goeun}")
    String authorization3;

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
    @DisplayName("get profile API Document")
    void getProfileTest() throws Exception {

        String handle = "dxxynni";

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v2/members/{handle}", handle)
                                .header("Authorization", authorization3)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("get-profile",
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
                                        fieldWithPath("result.handle").type(STRING).description("멤버 아이디"),
                                        fieldWithPath("result.profileImage").type(STRING).description("프로필 이미지"),
                                        fieldWithPath("result.name").type(STRING).description("멤버 이름"),
                                        fieldWithPath("result.message").type(STRING).description("한 줄 소개"),
                                        fieldWithPath("result.totalPostNum").type(NUMBER).description("총 태그된 게시물 개수"),
                                        fieldWithPath("result.followerCount").type(NUMBER).description("팔로워 수"),
                                        fieldWithPath("result.followingCount").type(NUMBER).description("팔로잉 수"),
                                        fieldWithPath("result.isFollow").type(BOOLEAN).description("현재 로그인한 멤버가 조회한 멤버를 팔로우하고 있는지의 여부 : 만약 본인이라면 null이 전달됩니다."),
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
                                        fieldWithPath("result.postList").type(ARRAY).description("태그된 게시물 리스트"),
                                        fieldWithPath("result.postList[].postId").type(NUMBER)
                                                .description("태그된 게시물 리스트: 게시물 아이디"),
                                        fieldWithPath("result.postList[].postImage").type(STRING)
                                                .description("태그된 게시물 리스트: 게시물 이미지")
                                )
                        )
                );
    }

    @Test
    @DisplayName("get uploaded Post API Document")
    void getUploadTest() throws Exception {

        String handle = "habongee";

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v2/members/{handle}/upload", handle)
                                .header("Authorization", authorization1)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("get-uploaded-post",
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
                                        fieldWithPath("result.postList").type(ARRAY).description("업로드한 게시물 리스트"),
                                        fieldWithPath("result.postList[].postId").type(NUMBER)
                                                .description("업로드한 게시물 리스트: 게시물 아이디"),
                                        fieldWithPath("result.postList[].postImage").type(STRING)
                                                .description("업로드한 게시물 리스트: 게시물 이미지")
                                )
                        )
                );
    }

    @Test
    @DisplayName("search Member API Document")
    void searchMemberTest() throws Exception {

        String keyword = "habo";

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v2/members/search")
                                .queryParam("keyword", keyword)
                                .header("Authorization", authorization1)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("search-member",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization").description("Basic auth credentials")
                                ),
                                queryParameters(
                                        parameterWithName("keyword").description("검색하고자 하는 멤버의 아이디"),
                                        parameterWithName("page").description("조회할 페이지 [default: 0]").optional()
                                ),
                                responseFields(
                                        fieldWithPath("isSuccess").type(BOOLEAN).description("성공 여부"),
                                        fieldWithPath("code").type(STRING).description("결과 코드"),
                                        fieldWithPath("message").type(STRING).description("결과 메세지"),
                                        fieldWithPath("result").type(OBJECT).description("결과 데이터"),
                                        fieldWithPath("result.pageInfo").type(OBJECT).description("검색된 멤버들 페이징 정보"),
                                        fieldWithPath("result.pageInfo.lastPage").type(BOOLEAN)
                                                .description(
                                                        "검색된 멤버들 페이징 정보: 현재 페이지가 마지막 페이지인지의 여부"
                                                ),
                                        fieldWithPath("result.pageInfo.totalPages").type(NUMBER)
                                                .description(
                                                        "검색된 멤버들 페이징 정보: 총 페이지 수"
                                                ),
                                        fieldWithPath("result.pageInfo.totalElements").type(NUMBER)
                                                .description(
                                                        "검색된 멤버들 페이징 정보: 총 검색된 멤버들의 수"
                                                ),
                                        fieldWithPath("result.pageInfo.size").type(NUMBER)
                                                .description(
                                                        "검색된 멤버들 페이징 정보: 페이징 사이즈"
                                                ),
                                        fieldWithPath("result.memberList").type(ARRAY).description("검색된 멤버 리스트"),
                                        fieldWithPath("result.memberList[].memberId").type(NUMBER)
                                                .description("검색된 멤버 리스트: 멤버 아이디").optional(),
                                        fieldWithPath("result.memberList[].profileImage").type(STRING)
                                                .description("검색된 멤버 리스트: 프로필 이미지").optional(),
                                        fieldWithPath("result.memberList[].handle").type(STRING)
                                                .description("검색된 멤버 리스트: 멤버 핸들").optional(),
                                        fieldWithPath("result.memberList[].name").type(STRING)
                                                .description("검색된 멤버 리스트: 멤버 이름").optional(),
                                        fieldWithPath("result.memberList[].isFollow").description("(이 데이터는 무시해주세요~ 어차피 전부 null로 전달됨!)").type(BOOLEAN).optional()
                                )
                        )
                );
    }
}