package com.apps.pochak.block.controller;

import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.block.domain.Block;
import com.apps.pochak.block.dto.response.BlockElements;
import com.apps.pochak.block.service.BlockService;
import com.apps.pochak.global.ControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;

import java.util.List;

import static com.apps.pochak.block.fixture.BlockFixture.BLOCK;
import static com.apps.pochak.global.ApiDocumentUtils.getDocumentRequest;
import static com.apps.pochak.global.ApiDocumentUtils.getDocumentResponse;
import static com.apps.pochak.global.converter.ListToPageConverter.toPage;
import static com.apps.pochak.member.fixture.MemberFixture.MEMBER1;
import static com.apps.pochak.member.fixture.MemberFixture.MEMBER2;
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
@WebMvcTest(BlockController.class)
@MockBean(JpaMetamodelMappingContext.class)
class BlockControllerTest extends ControllerTest {

    private static final List<Block> BLOCK_LIST = List.of(
            BLOCK
    );

    @MockBean
    BlockService blockService;

    @BeforeEach
    void setUp() {
        given(jwtProvider.validateAccessToken(any())).willReturn(true);
        given(jwtProvider.getSubject(any())).willReturn(MEMBER1.getId().toString());
        given(loginArgumentResolver.resolveArgument(any(), any(), any(), any()))
                .willReturn(Accessor.member(MEMBER1.getId()));
    }

    @Test
    @DisplayName("멤버를 차단한다.")
    void blockMemberTest() throws Exception {

        doNothing().when(blockService).blockMember(any(), any());

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .post("/api/v2/members/{handle}/block", MEMBER2.getHandle())
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
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
    @DisplayName("차단한 멤버들을 조회한다.")
    void getBlockedMemberTest() throws Exception {

        when(blockService.getBlockedMember(any(), any(), any()))
                .thenReturn(
                        new BlockElements(toPage(BLOCK_LIST))
                );

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .get("/api/v2/members/{handle}/block", MEMBER1.getHandle())
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
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
                                        fieldWithPath("result.blockList[].memberId").type(NUMBER)
                                                .description("차단한 사용자 리스트: 멤버 아이디").optional(),
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
    @DisplayName("멤버 차단을 취소한다.")
    void cancelBlockTest() throws Exception {

        doNothing().when(blockService).cancelBlock(any(), any(), any());

        this.mockMvc.perform(
                        RestDocumentationRequestBuilders
                                .delete("/api/v2/members/{handle}/block", MEMBER1.getHandle())
                                .queryParam("blockedMemberHandle", MEMBER2.getHandle())
                                .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN)
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