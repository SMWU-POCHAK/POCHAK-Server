package com.apps.pochak.login.controller;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.io.FileInputStream;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
public class LoginControllerTest {

    @Value("${test.authorization.master1}")
    String authorization1;

    @Value("${test.authorization.master2}")
    String authorization2;

    @Value("${test.authorization.master1.refresh}")
    String refreshToken;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    WebApplicationContext wac;


    @BeforeEach
    public void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    @Transactional
    @DisplayName("Signup API Document")
    void Signup() throws Exception {
        final String fileName = "APPS_LOGO";
        final String fileType = "PNG";

        final FileInputStream fileInputStream
                = new FileInputStream("src/test/resources/static/" + fileName + "." + fileType);
        final MockMultipartFile profileImage = new MockMultipartFile(
                "profileImage",
                fileName + "." + fileType,
                "multipart/form-data",
                fileInputStream
        );

        this.mockMvc.perform(
                        multipart("/api/v2/members/signup")
                                .file(profileImage)
                                .queryParam("name", "user1")
                                .queryParam("email", "user1@email.com")
                                .queryParam("handle", "user1")
                                .queryParam("message", "hi")
                                .queryParam("socialId", "1111111")
                                .queryParam("socialType", "apple")
                                .queryParam("socialRefreshToken", "1111111")
                ).andExpect(status().isOk())
                .andDo(
                        document("signup",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestParts(
                                        partWithName("profileImage").description("회원 프로필 사진 파일")
                                ),
                                queryParameters(
                                        parameterWithName("name").description("회원 이름"),
                                        parameterWithName("email").description("회원 이메일"),
                                        parameterWithName("handle").description("회원 닉네임"),
                                        parameterWithName("message").description("프로필 한 줄 소개"),
                                        parameterWithName("socialId").description("소셜 아이디"),
                                        parameterWithName("socialType").description("소셜 타입 (google, apple)"),
                                        parameterWithName("socialRefreshToken").description("애플 리프레쉬 토큰 (구글에는 해당되지 않음)")
                                ),
                                responseFields(
                                        fieldWithPath("isSuccess").type(BOOLEAN).description("성공 여부"),
                                        fieldWithPath("code").type(STRING).description("결과 코드"),
                                        fieldWithPath("message").type(STRING).description("결과 메세지"),
                                        fieldWithPath("result").type(OBJECT).description("결과 데이터"),
                                        fieldWithPath("result.id").type(NUMBER).description("멤버 아이디"),
                                        fieldWithPath("result.socialId").type(STRING).description("소셜 아이디"),
                                        fieldWithPath("result.name").type(STRING).description("회원 이름"),
                                        fieldWithPath("result.email").type(STRING).description("회원 이메일"),
                                        fieldWithPath("result.socialType").type(STRING).description("소셜 타입 (google, apple)"),
                                        fieldWithPath("result.accessToken").type(STRING).description("엑세스 토큰"),
                                        fieldWithPath("result.refreshToken").type(STRING).description("리프레쉬 토큰"),
                                        fieldWithPath("result.isNewMember").type(BOOLEAN).description("회원가입 여부")
                                )
                        )
                );
    }

    @Test
    @Transactional
    @DisplayName("Refresh API Document")
    void refresh() throws Exception {
        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .post("/api/v2/members/refresh")
                                .header("Authorization", authorization1)
                                .header("RefreshToken", refreshToken)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("refresh",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization")
                                                .description("Basic auth credentials")
                                ),
                                responseFields(
                                        fieldWithPath("isSuccess").type(BOOLEAN).description("성공 여부"),
                                        fieldWithPath("code").type(STRING).description("결과 코드"),
                                        fieldWithPath("message").type(STRING).description("결과 메세지"),
                                        fieldWithPath("result").type(OBJECT).description("결과 데이터"),
                                        fieldWithPath("result.accessToken").type(STRING).description("엑세스 토큰")
                                )
                        )
                );
    }

    @Test
    @Transactional
    @DisplayName("Logout API Document")
    void logout() throws Exception {
        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v2/members/logout")
                                .header("Authorization", authorization1)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("logout",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization")
                                                .description("Basic auth credentials")
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
    @DisplayName("Signout API Document")
    void signout() throws Exception {
        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .delete("/api/v2/members/signout")
                                .header("Authorization", authorization1)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("signout",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization")
                                                .description("Basic auth credentials")
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
