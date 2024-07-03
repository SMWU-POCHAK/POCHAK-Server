package com.apps.pochak.login.dto.apple.key;

import com.apps.pochak.global.api_payload.exception.handler.AuthenticationException;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.FAIL_VALIDATE_PUBLIC_KEY;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplePublicKeyResponse {

    private List<ApplePublicKey> keys;

    public ApplePublicKey getMatchedKeyBy(final String kid, final String alg) {
        return this.keys.stream()
                .filter(key -> key.getKid().equals(kid) && key.getAlg().equals(alg))
                .findAny()
                .orElseThrow(() -> new AuthenticationException(FAIL_VALIDATE_PUBLIC_KEY));
    }
}
