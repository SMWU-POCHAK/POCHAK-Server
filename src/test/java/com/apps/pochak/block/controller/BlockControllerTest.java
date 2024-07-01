package com.apps.pochak.block.controller;

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
class BlockControllerTest {
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
    @Transactional
    @DisplayName("Block Member API Document")
    void blockMemberTest() throws Exception {

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .post("/api/v2/members/{handle}/block", "goeun")
                                .header("Authorization", authorization)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("block-member",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization").description("Basic auth credentials")
                                ),
                                pathParameters(
                                        parameterWithName("handle").description("차단할 사용자의 아이디(handle) : 만약 로그인 정보와 일치할 경우 (= 자기 자신 차단 시도) BAD_REQUEST 에러가 발생합니다.")
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
    @DisplayName("Get BlockedMember API Document")
    void getBlockedMemberTest() throws Exception {

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v2/members/{handle}/block", "master1")
                                .header("Authorization", authorization)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("get-blocked-member",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization").description("Basic auth credentials")
                                ),
                                pathParameters(
                                        parameterWithName("handle").description("차단한 사용자의 아이디(handle) : " +
                                                "만약 로그인 정보와 일치하지 않을 경우 UNAUTHORIZED 에러가 발생합니다. (자신만 차단한 사용자 열람 가능)")
                                ),
                                queryParameters(
                                        parameterWithName("page").description("조회할 페이지 [default: 0]").optional()
                                ),
                                responseFields(
                                        fieldWithPath("isSuccess").type(BOOLEAN).description("성공 여부"),
                                        fieldWithPath("code").type(STRING).description("결과 코드"),
                                        fieldWithPath("message").type(STRING).description("결과 메세지"),
                                        fieldWithPath("result").type(OBJECT).description("결과 데이터"),
                                        fieldWithPath("result.pageInfo").type(OBJECT).description("차단한 사용자 페이징 정보"),
                                        fieldWithPath("result.pageInfo.lastPage").type(BOOLEAN)
                                                .description(
                                                        "차단한 사용자 페이징 정보: 현재 페이지가 마지막 페이지인지의 여부"
                                                ),
                                        fieldWithPath("result.pageInfo.totalPages").type(NUMBER)
                                                .description(
                                                        "차단한 사용자 페이징 정보: 총 페이지 수"
                                                ),
                                        fieldWithPath("result.pageInfo.totalElements").type(NUMBER)
                                                .description(
                                                        "차단한 사용자 페이징 정보: 총 차단된 사용자 수"
                                                ),
                                        fieldWithPath("result.pageInfo.size").type(NUMBER)
                                                .description(
                                                        "차단한 사용자 페이징 정보: 페이징 사이즈 [default: 30]"
                                                ),
                                        fieldWithPath("result.blockList").type(ARRAY).description("차단한 사용자 리스트"),
                                        fieldWithPath("result.blockList[].profileImage").type(STRING)
                                                .description("차단한 사용자 리스트: 프로필 이미지").optional(),
                                        fieldWithPath("result.blockList[].handle").type(STRING)
                                                .description("차단한 사용자 리스트: 아이디 (handle)").optional(),
                                        fieldWithPath("result.blockList[].name").type(STRING)
                                                .description("차단한 사용자 리스트: 이름").optional()
                                )

                        )
                );
    }

    @Test
    @Transactional
    @DisplayName("Cancel Block API Document")
    void cancelBlockTest() throws Exception {

        String blockedMemberHandle = "ssok";

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .delete("/api/v2/members/{handle}/block", "master1")
                                .queryParam("blockedMemberHandle", blockedMemberHandle)
                                .header("Authorization", authorization)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("cancel-block",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization").description("Basic auth credentials")
                                ),
                                pathParameters(
                                        parameterWithName("handle").description("차단한 사용자의 아이디(handle) " +
                                                ": 만약 로그인 정보와 일치하지 않을 경우 UNAUTHORIZED 에러가 발생합니다. (자신만 차단한 사용자 열람 가능)")
                                ),
                                queryParameters(
                                        parameterWithName("blockedMemberHandle").description("차단을 취소하고자하는 차단한 사용자 아이디").optional()
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