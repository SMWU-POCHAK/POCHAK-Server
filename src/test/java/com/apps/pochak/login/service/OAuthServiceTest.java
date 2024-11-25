package com.apps.pochak.login.service;

import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.login.dto.request.MemberInfoRequest;
import com.apps.pochak.member.service.MemberService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.DUPLICATE_HANDLE;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OAuthServiceTest {

    @Autowired
    OAuthService oAuthService;
    @Autowired
    MemberService memberService;

    @Autowired
    private EntityManager em;

    @Test
    void signup() {
        em.flush();

        // given
        assertDoesNotThrow(() -> memberService.checkDuplicate("handle"));
        assertDoesNotThrow(() -> memberService.checkDuplicate("handle"));
        MemberInfoRequest memberInfoRequest = new MemberInfoRequest("name", "email@gmail.com", "handle", "", "socialId1", null, "GOOGLE", "");
        MemberInfoRequest memberInfoRequest2 = new MemberInfoRequest("name", "email@gmail.com", "handle", "", "socialId2", null, "GOOGLE", "");

        // when
        assertDoesNotThrow(() -> oAuthService.signup(memberInfoRequest));
        GeneralException exception = assertThrows(GeneralException.class, () -> oAuthService.signup(memberInfoRequest2));

        // then
        assertEquals(DUPLICATE_HANDLE, exception.getCode());
    }
}