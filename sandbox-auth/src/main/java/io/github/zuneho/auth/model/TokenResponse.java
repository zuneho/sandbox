package io.github.zuneho.auth.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TokenResponse {
    private String token;
    private String tokenType;
    private Long expiresIn;
}
