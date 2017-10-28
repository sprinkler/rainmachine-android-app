package com.rainmachine.data.remote.sprinkler.v3.mapper;

import com.rainmachine.data.remote.sprinkler.v3.response.UpdateResponse3;
import com.rainmachine.data.remote.util.ApiMapperException;
import com.rainmachine.domain.model.Update;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

public class UpdateResponseMapper3 implements Function<UpdateResponse3, Update> {

    private static volatile UpdateResponseMapper3 instance;

    public static UpdateResponseMapper3 instance() {
        if (instance == null) {
            instance = new UpdateResponseMapper3();
        }
        return instance;
    }

    @Override
    public Update apply(@NonNull UpdateResponse3 updateResponse) throws Exception {
        if (updateResponse == null) {
            throw new ApiMapperException();
        }
        Update update = new Update();
        update.currentVersion = updateResponse.currentVersion;
        update.lastUpdateCheckTimestamp = updateResponse.lastUpdateCheck;
        update.newVersion = updateResponse.newVersion;
        // Apart from status 0, I do not know which other values there are. Set to error for now.
        update.status = updateResponse.updateStatus == 0 ? Update.Status.IDLE : Update.Status.ERROR;
        update.update = updateResponse.update;
        return update;
    }
}
