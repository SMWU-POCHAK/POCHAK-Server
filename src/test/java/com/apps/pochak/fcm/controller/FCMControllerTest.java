package com.apps.pochak.fcm.controller;

import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.fcm.dto.FCMToken;
import com.apps.pochak.fcm.service.FCMService;
import com.apps.pochak.global.ControllerTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;

import static com.apps.pochak.global.ApiDocumentUtils.getDocumentRequest;
import static com.apps.pochak.global.ApiDocumentUtils.getDocumentResponse;
import static com.apps.pochak.member.fixture.MemberFixture.MEMBER1;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureRestDocs
@WebMvcTest(FCMController.class)
@MockBean(JpaMetamodelMappingContext.class)
class FCMControllerTest extends ControllerTest {

    private static final String FCM_TOKEN = "FCMToken";

    @MockBean
    FCMService fcmService;

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
    @DisplayName("회원의 FCM 토큰을 저장한다.")
    void saveFCMToken() throws Exception {
        doNothing().when(fcmService).saveToken(any(), any());

        final FCMToken fcmToken = new FCMToken(FCM_TOKEN);

        this.mockMvc.perform(
                        post("/api/v1/fcm/register")
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
                                .content(objectMapper.writeValueAsString(fcmToken))
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("save-fcm-token",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization").description("Basic auth credentials")
                                ),
                                requestFields(
                                        fieldWithPath("token").type(STRING).description("FCM Token")
                                ),
                                responseFields(
                                        fieldWithPath("isSuccess").type(BOOLEAN).description("성공 여부"),
                                        fieldWithPath("code").type(STRING).description("결과 코드"),
                                        fieldWithPath("message").type(STRING)
                                                .description("결과 메세지: `성공적으로 토큰을 등록하였습니다.`")
                                )
                        )
                );
    }
}