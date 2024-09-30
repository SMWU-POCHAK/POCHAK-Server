package com.apps.pochak.alarm.controller;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.alarm.domain.TagAlarm;
import com.apps.pochak.alarm.dto.response.AlarmElements;
import com.apps.pochak.alarm.service.AlarmService;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.global.ControllerTest;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.dto.response.PostPreviewResponse;
import com.apps.pochak.post.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;

import java.util.List;

import static com.apps.pochak.alarm.fixture.AlarmFixture.*;
import static com.apps.pochak.global.ApiDocumentUtils.getDocumentRequest;
import static com.apps.pochak.global.ApiDocumentUtils.getDocumentResponse;
import static com.apps.pochak.global.api_payload.code.status.SuccessStatus.SUCCESS_CHECK_ALARM;
import static com.apps.pochak.global.converter.ListToPageConverter.toPage;
import static com.apps.pochak.member.fixture.MemberFixture.STATIC_MEMBER1;
import static com.apps.pochak.tag.fixture.TagFixture.STATIC_WAITING_TAG;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureRestDocs
@WebMvcTest(AlarmController.class)
@MockBean(JpaMetamodelMappingContext.class)
class AlarmControllerTest extends ControllerTest {

    private static final Member MEMBER = STATIC_MEMBER1;
    private static final TagAlarm TAG_ALARM = STATIC_TAG_ALARM;

    private static final List<Alarm> ALARM_LIST = List.of(
            STATIC_COMMENT_REPLY_ALARM,
            STATIC_FOLLOW_ALARM,
            STATIC_TAGGED_LIKE_ALARM,
            STATIC_TAG_ALARM
    );

    @MockBean
    AlarmService alarmService;

    @MockBean
    PostService postService;

    @BeforeEach
    void setUp() {
        given(jwtProvider.validateAccessToken(any())).willReturn(true);
        given(jwtProvider.getSubject(any())).willReturn(MEMBER.getId().toString());
        given(loginArgumentResolver.resolveArgument(any(), any(), any(), any()))
                .willReturn(Accessor.member(MEMBER.getId()));
    }

    @Test
    @DisplayName("로그인한 회원의 알람을 전부 조회한다.")
    void getAllAlarmsTest() throws Exception {

        when(alarmService.getAllAlarms(any(), any()))
                .thenReturn(
                        new AlarmElements(toPage(ALARM_LIST))
                );

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v2/alarms")
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("get-alarms",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName(ACCESS_TOKEN_HEADER).description("Basic auth credentials")
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
    @DisplayName("태그 알람에서 게시물 미리보기를 조회한다.")
    void getPreviewPostTest() throws Exception {

        when(postService.getPreviewPost(any(), any()))
                .thenReturn(
                        new PostPreviewResponse(TAG_ALARM.getTag().getPost(), List.of(STATIC_WAITING_TAG))
                );

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v2/alarms/{alarmId}", TAG_ALARM.getId())
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
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
    @DisplayName("알람을 확인하고 비공개 처리한다.")
    void checkAlarmTest() throws Exception {

        when(alarmService.checkAlarm(any(), any())).thenReturn(SUCCESS_CHECK_ALARM);

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .post("/api/v2/alarms/{alarmId}", TAG_ALARM.getId())
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
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