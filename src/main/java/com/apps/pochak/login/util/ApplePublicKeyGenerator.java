package com.apps.pochak.login.util;

import com.apps.pochak.global.api_payload.exception.handler.AppleOAuthException;
import com.apps.pochak.login.dto.apple.ApplePublicKey;
import com.apps.pochak.login.dto.apple.ApplePublicKeyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;

import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.INVALID_KEY_SPEC;
import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.NO_SUCH_ALGORITHM;

@Component
@RequiredArgsConstructor
public class ApplePublicKeyGenerator {
    public PublicKey generatePublicKey(
            final Map<String, String> tokenHeaders,
            final ApplePublicKeyResponse response
    ) {
        ApplePublicKey publicKey = response.getMatchedKeyBy(
                tokenHeaders.get("kid"),
                tokenHeaders.get("alg")
        );

        return getPublicKey(publicKey);
    }

    private PublicKey getPublicKey(final ApplePublicKey publicKey) {
        byte[] nBytes = Base64.getUrlDecoder().decode(publicKey.getN());
        byte[] eBytes = Base64.getUrlDecoder().decode(publicKey.getE());

        BigInteger n = new BigInteger(1, nBytes);
        BigInteger e = new BigInteger(1, eBytes);

        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(n, e);

        try {
            KeyFactory keyFactory = KeyFactory.getInstance(publicKey.getKty());
            return keyFactory.generatePublic(publicKeySpec);
        } catch (NoSuchAlgorithmException exception) {
            throw new AppleOAuthException(NO_SUCH_ALGORITHM);
        } catch (InvalidKeySpecException exception) {
            throw new AppleOAuthException(INVALID_KEY_SPEC);
        }
    }
}
