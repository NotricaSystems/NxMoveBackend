package com.next.move.security;

import lombok.Data;

@Data
public class LoginRequest {

    private String username;
    private String password;

    // Optional: for “remember me” checkbox
    private boolean rememberMe;

    public LoginRequest() {}

    public LoginRequest(String username, String password, boolean rememberMe) {
        this.username = username;
        this.password = password;
        this.rememberMe = rememberMe;
    }

}

