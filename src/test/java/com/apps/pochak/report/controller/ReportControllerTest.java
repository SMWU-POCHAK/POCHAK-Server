package com.apps.pochak.report.controller;

import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.global.ControllerTest;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.report.domain.ReportType;
import com.apps.pochak.report.dto.request.ReportUploadRequest;
import com.apps.pochak.report.service.ReportService;
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

import static com.apps.pochak.global.ApiDocumentUtils.getDocumentRequest;
import static com.apps.pochak.global.ApiDocumentUtils.getDocumentResponse;
import static com.apps.pochak.member.fixture.MemberFixture.STATIC_MEMBER1;
import static com.apps.pochak.post.fixture.PostFixture.STATIC_PUBLIC_POST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureRestDocs
@WebMvcTest(ReportController.class)
@MockBean(JpaMetamodelMappingContext.class)
class ReportControllerTest extends ControllerTest {
    private static final Member MEMBER1 = STATIC_MEMBER1;
    private static final Post PUBLIC_POST = STATIC_PUBLIC_POST;

    @MockBean
    ReportService reportService;

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
    @DisplayName("게시물을 신고한다.")
    void uploadReportTest() throws Exception {
        final ReportUploadRequest uploadRequest = new ReportUploadRequest(
                PUBLIC_POST.getId(),
                ReportType.NOT_INTERESTED
        );

        doNothing().when(reportService).saveReport(any(), any());

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .post("/api/v1/reports")
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
                                .content(objectMapper.writeValueAsString(uploadRequest))
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("upload-report",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization")
                                                .description(
                                                        "Basic auth credentials"
                                                )
                                ),
                                requestFields(
                                        fieldWithPath("postId").type(NUMBER).description("신고된 게시물 아이디"),
                                        fieldWithPath("reportType").type(STRING).description("신고 유형")
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