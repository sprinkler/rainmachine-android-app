package com.rainmachine.data.remote.sprinkler.v4.mapper;

import com.rainmachine.data.remote.sprinkler.v4.response.LoginResponse;
import com.rainmachine.domain.model.Login;
import com.rainmachine.domain.model.LoginStatus;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

public class LoginResponseMapper implements Function<LoginResponse, Login> {

    private static volatile LoginResponseMapper instance;

    public static LoginResponseMapper instance() {
        if (instance == null) {
            instance = new LoginResponseMapper();
        }
        return instance;
    }

    @Override
    public Login apply(@NonNull LoginResponse response) throws Exception {
        return new Login(response.accessToken, LoginStatus.SUCCESS);
    }
}
