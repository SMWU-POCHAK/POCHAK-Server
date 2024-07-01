package com.apps.pochak.report.controller;

import com.apps.pochak.report.domain.ReportType;
import com.apps.pochak.report.dto.request.ReportUploadRequest;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
class ReportControllerTest {

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
    @DisplayName("Report Upload API Document")
    void uploadReportTest() throws Exception {

        final ReportUploadRequest uploadRequest = new ReportUploadRequest(174L, ReportType.NOT_INTERESTED);

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .post("/api/v1/reports")
                                .header("Authorization", authorization)
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