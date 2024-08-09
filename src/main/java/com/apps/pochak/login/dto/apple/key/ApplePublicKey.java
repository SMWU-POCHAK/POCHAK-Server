package com.apps.pochak.login.dto.apple.key;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ApplePublicKey {
    @JsonProperty
    private String kty;

    @JsonProperty
    private String kid;

    @JsonProperty
    private String use;

    @JsonProperty
    private String alg;

    @JsonProperty
    private String n;

    @JsonProperty
    private String e;
}
