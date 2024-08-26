package com.apps.pochak.tag.controller;

import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.global.ControllerTest;
import com.apps.pochak.tag.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;

import static com.apps.pochak.global.ApiDocumentUtils.getDocumentRequest;
import static com.apps.pochak.global.ApiDocumentUtils.getDocumentResponse;
import static com.apps.pochak.global.api_payload.code.status.SuccessStatus.SUCCESS_ACCEPT;
import static com.apps.pochak.member.fixture.MemberFixture.MEMBER1;
import static com.apps.pochak.tag.fixture.TagFixture.WAITING_TAG;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureRestDocs
@WebMvcTest(TagController.class)
@MockBean(JpaMetamodelMappingContext.class)
class TagControllerTest extends ControllerTest {

    @MockBean
    TagService tagService;

    @BeforeEach
    void setUp() {
        given(jwtProvider.validateAccessToken(any())).willReturn(true);
        given(jwtProvider.getSubject(any())).willReturn(MEMBER1.getId().toString());
        given(loginArgumentResolver.resolveArgument(any(), any(), any(), any()))
                .willReturn(Accessor.member(MEMBER1.getId()));
    }

    @Test
    @DisplayName("태그를 수락한다.")
    void approveTagTest() throws Exception {

        Boolean isAccept = true;

        when(tagService.approveOrRejectTagRequest(any(), any(), any()))
                .thenReturn(SUCCESS_ACCEPT);

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .post("/api/v2/tags/{tagId}", WAITING_TAG.getId())
                                .queryParam("isAccept", isAccept.toString())
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("approve-tag",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization").description("Basic auth credentials")
                                ),
                                queryParameters(
                                        parameterWithName("isAccept").description("수락 여부")
                                ),
                                pathParameters(
                                        parameterWithName("tagId").description("수락할 태그 아이디")
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