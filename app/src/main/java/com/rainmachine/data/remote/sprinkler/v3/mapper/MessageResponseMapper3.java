package com.rainmachine.data.remote.sprinkler.v3.mapper;

import com.rainmachine.data.remote.sprinkler.v3.response.MessageResponse3;
import com.rainmachine.domain.util.Irrelevant;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;


public class MessageResponseMapper3 implements Function<MessageResponse3, Irrelevant> {

    private static volatile MessageResponseMapper3 instance;

    public static MessageResponseMapper3 instance() {
        if (instance == null) {
            instance = new MessageResponseMapper3();
        }
        return instance;
    }

    @Override
    public Irrelevant apply(@NonNull MessageResponse3 messageResponse3) throws Exception {
        return Irrelevant.INSTANCE;
    }
}
