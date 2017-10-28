package com.rainmachine.data.remote.sprinkler.v3;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.rainmachine.data.remote.sprinkler.v3.response.BaseResponse3;
import com.rainmachine.domain.util.Strings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import retrofit2.HttpException;
import retrofit2.Response;
import timber.log.Timber;

public class SprinklerApiUtils3 {

    private Gson gson;

    public SprinklerApiUtils3(Gson gson) {
        this.gson = gson;
    }

    public boolean isAuthenticationFailure(HttpException httpException) {
        BaseResponse3 response = getSprinklerResponse(httpException);
        return response != null && (response.statusCode == BaseResponse3.SC_SESSION_EXPIRED ||
                response.statusCode == BaseResponse3.SC_MODIFIED_SESSION || !Strings.isBlank
                (response.status));
    }

    private BaseResponse3 getSprinklerResponse(HttpException httpException) {
        Response response = httpException.response();
        if (response != null && response.errorBody() != null) {
            BufferedReader reader;
            StringBuilder sb = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(response.errorBody().byteStream()));
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                Timber.w(e, e.getMessage());
            }
            String result = sb.toString();
            try {
                return gson.fromJson(result, BaseResponse3.class);
            } catch (JsonSyntaxException jse) {
                return null;
            }
        }
        return null;
    }
}
