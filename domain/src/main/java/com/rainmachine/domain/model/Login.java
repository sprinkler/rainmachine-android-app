package com.rainmachine.domain.model;

public class Login {
    public String accessToken;
    public LoginStatus status;

    public Login(String accessToken, LoginStatus status) {
        this.accessToken = accessToken;
        this.status = status;
    }

    public Login(LoginStatus status) {
        this.status = status;
    }
}
