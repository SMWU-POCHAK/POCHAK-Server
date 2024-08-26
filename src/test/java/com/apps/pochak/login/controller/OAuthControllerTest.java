package com.apps.pochak.login.controller;

import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.global.ControllerTest;
import com.apps.pochak.login.dto.response.AccessTokenResponse;
import com.apps.pochak.login.dto.response.OAuthMemberResponse;
import com.apps.pochak.login.service.AppleOAuthService;
import com.apps.pochak.login.service.GoogleOAuthService;
import com.apps.pochak.login.service.OAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;

import java.io.FileInputStream;

import static com.apps.pochak.global.ApiDocumentUtils.getDocumentRequest;
import static com.apps.pochak.global.ApiDocumentUtils.getDocumentResponse;
import static com.apps.pochak.global.MockMultipartFileConverter.getSampleMultipartFile;
import static com.apps.pochak.member.fixture.MemberFixture.MEMBER1;
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
@WebMvcTest(OAuthController.class)
@MockBean(JpaMetamodelMappingContext.class)
public class OAuthControllerTest extends ControllerTest {

    @MockBean
    OAuthService oAuthService;

    @MockBean
    AppleOAuthService appleOAuthService;

    @MockBean
    GoogleOAuthService googleOAuthService;

    @BeforeEach
    void setUp() {
        given(jwtProvider.validateAccessToken(any())).willReturn(true);
        given(jwtProvider.getSubject(any())).willReturn(MEMBER1.getId().toString());
        given(loginArgumentResolver.resolveArgument(any(), any(), any(), any()))
                .willReturn(Accessor.member(MEMBER1.getId()));
    }

    @Test
    @DisplayName("회원가입을 한다.")
    void Signup() throws Exception {
        when(oAuthService.signup(any()))
                .thenReturn(new OAuthMemberResponse(MEMBER1, false, ACCESS_TOKEN));

        this.mockMvc.perform(
                        multipart("/api/v2/signup")
                                .file(getSampleMultipartFile())
                                .queryParam("name", MEMBER1.getName())
                                .queryParam("email", MEMBER1.getEmail())
                                .queryParam("handle", MEMBER1.getHandle())
                                .queryParam("message", MEMBER1.getMessage())
                                .queryParam("socialId", MEMBER1.getSocialId())
                                .queryParam("socialType", MEMBER1.getSocialType().toString())
                                .queryParam("socialRefreshToken", MEMBER1.getSocialRefreshToken())
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
                                        fieldWithPath("result.handle").type(STRING).description("회원 아이디 (핸들)"),
                                        fieldWithPath("result.socialType").type(STRING).description("소셜 타입 (google, apple)"),
                                        fieldWithPath("result.accessToken").type(STRING).description("엑세스 토큰"),
                                        fieldWithPath("result.refreshToken").type(STRING).description("리프레쉬 토큰"),
                                        fieldWithPath("result.isNewMember").type(BOOLEAN).description("회원가입 여부")
                                )
                        )
                );
    }

    @Test
    @DisplayName("리프레시 토큰으로 액세스 토큰을 재발급한다.")
    void refresh() throws Exception {

        when(oAuthService.reissueAccessToken())
                .thenReturn(
                        AccessTokenResponse.builder()
                                .accessToken(ACCESS_TOKEN)
                                .build()
                );

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .post("/api/v2/refresh")
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
                                .header(REFRESH_TOKEN_HEADER, REFRESH_TOKEN)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("refresh",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization")
                                                .description("Basic auth credentials"),
                                        headerWithName("RefreshToken")
                                                .description("리프레시 토큰")
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
    @DisplayName("로그아웃을 한다.")
    void logout() throws Exception {

        doNothing().when(oAuthService).logout(any());

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v2/logout")
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
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
    @DisplayName("회원 탈퇴를 한다.")
    void signout() throws Exception {

        doNothing().when(oAuthService).signout(any());

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .delete("/api/v2/signout")
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
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
