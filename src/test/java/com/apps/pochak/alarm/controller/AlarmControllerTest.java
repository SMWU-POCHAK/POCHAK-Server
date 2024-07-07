package com.apps.pochak.alarm.controller;

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
import org.springframework.transaction.annotation.Transactional;
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
class AlarmControllerTest {

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
    @DisplayName("Get Alarms API Document")
    void getAllAlarmsTest() throws Exception {

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v2/alarms")
                                .header("Authorization", authorization1)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("get-alarms",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization").description("Basic auth credentials")
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
                                        fieldWithPath("result.alarmList").type(ARRAY).description("알람 리스트"),
                                        fieldWithPath("result.alarmList[].alarmId").type(NUMBER)
                                                .description("[공통] 알람 리스트 | 알람 아이디").optional(),
                                        fieldWithPath("result.alarmList[].alarmType").type(STRING)
                                                .description("[공통] 알람 리스트 | 알람 종류").optional(),
                                        fieldWithPath("result.alarmList[].isChecked").type(BOOLEAN)
                                                .description("[공통] 알람 리스트 | 알람 확인 여부").optional(),
                                        fieldWithPath("result.alarmList[].tagId").type(NUMBER)
                                                .description("[태그 알람] 알람 리스트 | 태그 아이디").optional(),
                                        fieldWithPath("result.alarmList[].ownerId").type(NUMBER)
                                                .description("[태그 알람] 알람 리스트 | 포착한 유저 아이디").optional(),
                                        fieldWithPath("result.alarmList[].ownerHandle").type(STRING)
                                                .description("[태그 알람] 알람 리스트 | 포착한 유저 핸들").optional(),
                                        fieldWithPath("result.alarmList[].ownerName").type(STRING)
                                                .description("[태그 알람] 알람 리스트 | 포착한 유저 이름").optional(),
                                        fieldWithPath("result.alarmList[].ownerProfileImage").type(STRING)
                                                .description("[태그 알람] 알람 리스트 | 포착한 유저 프로필 사진").optional(),
                                        fieldWithPath("result.alarmList[].postId").type(NUMBER)
                                                .description("[태그 알람] 알람 리스트 | 게시물 아이디").optional(),
                                        fieldWithPath("result.alarmList[].postImage").type(STRING)
                                                .description("[태그 알람] 알람 리스트 | 게시물 사진").optional(),
                                        fieldWithPath("result.alarmList[].memberId").type(NUMBER)
                                                .description("[좋아요 알람] 알람 리스트 | 좋아요한 유저 아이디").optional(),
                                        fieldWithPath("result.alarmList[].memberHandle").type(STRING)
                                                .description("[좋아요 알람] 알람 리스트 | 좋아요한 유저 핸들").optional(),
                                        fieldWithPath("result.alarmList[].memberName").type(STRING)
                                                .description("[좋아요 알람] 알람 리스트 | 좋아요한 유저 이름").optional(),
                                        fieldWithPath("result.alarmList[].memberProfileImage").type(STRING)
                                                .description("[좋아요 알람] 알람 리스트 | 좋아요한 유저 프로필 사진").optional(),
                                        fieldWithPath("result.alarmList[].postId").type(NUMBER)
                                                .description("[좋아요 알람] 알람 리스트 | 좋아요 눌린 게시물 이미지").optional(),
                                        fieldWithPath("result.alarmList[].postImage").type(STRING)
                                                .description("[좋아요 알람] 알람 리스트 | 좋아요 눌린 게시물 사진").optional(),
                                        fieldWithPath("result.alarmList[].memberId").type(NUMBER)
                                                .description("[팔로우 알람] 알람 리스트 | 팔로우한 유저 아이디").optional(),
                                        fieldWithPath("result.alarmList[].memberHandle").type(STRING)
                                                .description("[팔로우 알람] 알람 리스트 | 팔로우한 유저 핸들").optional(),
                                        fieldWithPath("result.alarmList[].memberName").type(STRING)
                                                .description("[팔로우 알람] 알람 리스트 | 팔로우한 유저 이름").optional(),
                                        fieldWithPath("result.alarmList[].memberProfileImage").type(STRING)
                                                .description("[팔로우 알람] 알람 리스트 | 팔로우한 유저 프로필 사진").optional(),
                                        fieldWithPath("result.alarmList[].commentId").type(NUMBER)
                                                .description("[댓글 알람] 알람 리스트 | 댓글 아이디").optional(),
                                        fieldWithPath("result.alarmList[].commentContent").type(STRING)
                                                .description("[댓글 알람] 알람 리스트 | 댓글 내용").optional(),
                                        fieldWithPath("result.alarmList[].postId").type(NUMBER)
                                                .description("[댓글 알람] 알람 리스트 | 댓글이 달린 게시물 아이디").optional(),
                                        fieldWithPath("result.alarmList[].postImage").type(STRING)
                                                .description("[댓글 알람] 알람 리스트 | 댓글이 달린 게시물 이미지").optional(),
                                        fieldWithPath("result.alarmList[].memberId").type(NUMBER)
                                                .description("[댓글 알람] 알람 리스트 | 댓글 단 유저 아이디").optional(),
                                        fieldWithPath("result.alarmList[].memberHandle").type(STRING)
                                                .description("[댓글 알람] 알람 리스트 | 댓글 단 유저 핸들").optional(),
                                        fieldWithPath("result.alarmList[].memberName").type(STRING)
                                                .description("[댓글 알람] 알람 리스트 | 댓글 단 유저 이름").optional(),
                                        fieldWithPath("result.alarmList[].memberProfileImage").type(STRING)
                                                .description("[댓글 알람] 알람 리스트 | 댓글 단 유저 프로필 사진").optional()
                                )

                        )
                );
    }

    @Test
    @DisplayName("Get preview post API Document")
    void getPreviewPostTest() throws Exception {
        Long alarmId = 1485L;

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v2/alarms/{alarmId}", alarmId)
                                .header("Authorization", authorization2)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("get-preview-post",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization").description("Basic auth credentials")
                                ),
                                pathParameters(
                                        parameterWithName("alarmId").description("알람 아이디")
                                ),
                                responseFields(
                                        fieldWithPath("isSuccess").type(BOOLEAN).description("성공 여부"),
                                        fieldWithPath("code").type(STRING).description("결과 코드"),
                                        fieldWithPath("message").type(STRING).description("결과 메세지"),
                                        fieldWithPath("result").type(OBJECT).description("결과 데이터"),
                                        fieldWithPath("result.ownerId").type(NUMBER).description("포착한 유저의 아이디"),
                                        fieldWithPath("result.ownerHandle").type(STRING).description("포착한 유저의 핸들"),
                                        fieldWithPath("result.ownerProfileImage").type(STRING).description("포착한 유저의 프로필 이미지"),
                                        fieldWithPath("result.tagList").type(ARRAY).description("태그된 리스트"),
                                        fieldWithPath("result.tagList[].memberId").type(NUMBER).description("태그된 리스트 | 유저 아이디"),
                                        fieldWithPath("result.tagList[].profileImage").type(STRING).description("태그된 리스트 | 유저 프로필 이미지"),
                                        fieldWithPath("result.tagList[].handle").type(STRING).description("태그된 리스트 | 유저 핸들"),
                                        fieldWithPath("result.tagList[].name").type(STRING).description("태그된 리스트 | 유저 이름"),
                                        fieldWithPath("result.postImage").type(STRING).description("게시물 미리보기 이미지")
                                )

                        )
                );
    }

    @Test
    @Transactional
    @DisplayName("Check Alarms API Document")
    void checkAlarmTest() throws Exception {
        Long alarmId = 1485L;

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .post("/api/v2/alarms/{alarmId}", alarmId)
                                .header("Authorization", authorization2)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("check-alarm",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization").description("Basic auth credentials")
                                ),
                                pathParameters(
                                        parameterWithName("alarmId").description("알람 아이디")
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