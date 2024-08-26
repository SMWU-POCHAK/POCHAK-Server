package com.apps.pochak.member.controller;

import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.global.ControllerTest;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.dto.response.MemberElements;
import com.apps.pochak.member.dto.response.ProfileResponse;
import com.apps.pochak.member.dto.response.ProfileUpdateResponse;
import com.apps.pochak.member.service.MemberService;
import com.apps.pochak.post.dto.PostElements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static com.apps.pochak.global.ApiDocumentUtils.getDocumentRequest;
import static com.apps.pochak.global.ApiDocumentUtils.getDocumentResponse;
import static com.apps.pochak.global.MockMultipartFileConverter.getSampleMultipartFile;
import static com.apps.pochak.global.converter.ListToPageConverter.toPage;
import static com.apps.pochak.member.fixture.MemberFixture.MEMBER1;
import static com.apps.pochak.member.fixture.MemberFixture.MEMBER2;
import static com.apps.pochak.post.fixture.PostFixture.PUBLIC_POST;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureRestDocs
@WebMvcTest(MemberController.class)
@MockBean(JpaMetamodelMappingContext.class)
class MemberControllerTest extends ControllerTest {

    private static final List<Member> MEMBER_LIST = List.of(
            MEMBER1,
            MEMBER2
    );

    @MockBean
    MemberService memberService;

    @BeforeEach
    void setUp() {
        given(jwtProvider.validateAccessToken(any())).willReturn(true);
        given(jwtProvider.getSubject(any())).willReturn(MEMBER1.getId().toString());
        given(loginArgumentResolver.resolveArgument(any(), any(), any(), any()))
                .willReturn(Accessor.member(MEMBER1.getId()));
    }

    @Test
    @DisplayName("프로필 탭을 조회한다.")
    void getProfileTest() throws Exception {

        when(memberService.getProfileDetail(any(), any(), any()))
                .thenReturn(
                        ProfileResponse.of()
                                .member(MEMBER1)
                                .postPage(toPage(List.of(PUBLIC_POST)))
                                .followerCount(1)
                                .followingCount(1)
                                .isFollow(null)
                                .build()
                );

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v2/members/{handle}", MEMBER1.getHandle())
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
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
                                        fieldWithPath("result.isFollow").type(BOOLEAN)
                                                .description("현재 로그인한 멤버가 조회한 멤버를 팔로우하고 있는지의 여부 : 만약 본인이라면 null이 전달됩니다.")
                                                .optional(),
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
                                                .description("태그된 게시물 리스트: 게시물 아이디").optional(),
                                        fieldWithPath("result.postList[].postImage").type(STRING)
                                                .description("태그된 게시물 리스트: 게시물 이미지").optional()
                                )
                        )
                );
    }

    @Test
    @DisplayName("프로필 탭에서 업로드한 게시물 목록을 조회한다.")
    void getUploadTest() throws Exception {

        when(memberService.getUploadPosts(any(), any(), any()))
                .thenReturn(
                        PostElements.from(toPage(List.of(PUBLIC_POST)))
                );

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v2/members/{handle}/upload", MEMBER1.getHandle())
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
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
    @DisplayName("회원 정보를 수정한다.")
    void updateProfileTest() throws Exception {
        when(memberService.updateProfile(any(), any(), any()))
                .thenReturn(
                        ProfileUpdateResponse
                                .builder()
                                .member(MEMBER1)
                                .build()
                );

        MockMultipartHttpServletRequestBuilder builder =
                RestDocumentationRequestBuilders.
                        multipart("/api/v2/members/{handle}", MEMBER1.getHandle());

        builder.with(
                new RequestPostProcessor() {
                    @Override
                    public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                        request.setMethod("PUT");
                        return request;
                    }
                });

        this.mockMvc.perform(
                        builder
                                .file("profileImage", getSampleMultipartFile().getBytes())
                                .queryParam("name", MEMBER1.getName())
                                .queryParam("message", MEMBER1.getMessage())
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("update-profile",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                requestHeaders(
                                        headerWithName("Authorization").description("Basic auth credentials")
                                ),
                                requestParts(
                                        partWithName("profileImage").description("회원 프로필 이미지")
                                ),
                                queryParameters(
                                        parameterWithName("name").description("회원 이름"),
                                        parameterWithName("message").description("프로필 한 줄 소개")
                                ),
                                responseFields(
                                        fieldWithPath("isSuccess").type(BOOLEAN).description("성공 여부"),
                                        fieldWithPath("code").type(STRING).description("결과 코드"),
                                        fieldWithPath("message").type(STRING).description("결과 메세지"),
                                        fieldWithPath("result").type(OBJECT).description("결과 데이터"),
                                        fieldWithPath("result.name").type(STRING).description("회원 이름"),
                                        fieldWithPath("result.handle").type(STRING).description("회원 아이디 (handle)"),
                                        fieldWithPath("result.message").type(STRING).description("메세지"),
                                        fieldWithPath("result.profileImage").type(STRING).description("프로필 사진")
                                )
                        )
                );
    }

    @Test
    @DisplayName("아이디 혹은 이름을 통해 회원을 검색한다.")
    void searchMemberTest() throws Exception {

        when(memberService.search(any(), any(), any()))
                .thenReturn(
                        MemberElements.from(toPage(MEMBER_LIST))
                );

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v2/members/search")
                                .queryParam("keyword", "me")
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
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

    @Test
    @DisplayName("중복되는 아이디(handle)가 있는지 확인한다.")
    void checkDuplicateHandleTest() throws Exception {

        doNothing().when(memberService).checkDuplicate(any());

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v2/members/duplicate")
                                .queryParam("handle", MEMBER1.getHandle())
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(
                        document("check-duplicate-handle",
                                getDocumentRequest(),
                                getDocumentResponse(),
                                queryParameters(
                                        parameterWithName("handle").description("검색하고자 하는 handle")
                                ),
                                responseFields(
                                        fieldWithPath("isSuccess").type(BOOLEAN).description("성공 여부 (중복시 실패 처리됨)"),
                                        fieldWithPath("code").type(STRING).description("결과 코드"),
                                        fieldWithPath("message").type(STRING).description("결과 메세지")
                                )
                        )
                );
    }
}