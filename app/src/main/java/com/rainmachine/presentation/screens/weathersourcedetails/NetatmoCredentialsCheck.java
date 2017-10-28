package com.rainmachine.presentation.screens.weathersourcedetails;

public class NetatmoCredentialsCheck {
    public String username;
    public String password;
    public boolean isValid;

    public NetatmoCredentialsCheck(String username, String password, boolean isValid) {
        this.username = username;
        this.password = password;
        this.isValid = isValid;
    }
}
