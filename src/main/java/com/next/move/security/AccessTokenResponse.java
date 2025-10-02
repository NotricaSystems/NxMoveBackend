package com.next.move.security;

import lombok.Getter;

@Getter
public class AccessTokenResponse {
    private String accessToken;
    private long expiresIn;

    public AccessTokenResponse(String accessToken, long expiresIn) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
    }

}

