package com.rainmachine.data.remote.sprinkler.v4.mapper;

import com.rainmachine.data.remote.sprinkler.v4.response.ParserSingleResponse;
import com.rainmachine.data.remote.util.ApiMapperException;
import com.rainmachine.domain.model.Parser;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;


public class ParserSingleResponseMapper implements Function<ParserSingleResponse, Parser> {

    private static volatile ParserSingleResponseMapper instance;

    public static ParserSingleResponseMapper instance() {
        if (instance == null) {
            instance = new ParserSingleResponseMapper();
        }
        return instance;
    }

    @Override
    public Parser apply(@NonNull ParserSingleResponse response) throws Exception {
        if (response == null || response.parser == null) {
            throw new ApiMapperException();
        }
        return ParsersResponseMapper.convertParserResponse(response.parser);
    }
}