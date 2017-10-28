package com.rainmachine.data.boundary;

import com.rainmachine.data.remote.cloud.CloudSprinklersApiDelegate;
import com.rainmachine.data.remote.cloud.CloudValidateApiDelegate;
import com.rainmachine.domain.boundary.data.CloudRepository;
import com.rainmachine.domain.model.CloudEntry;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import kotlin.Pair;

public class CloudRepositoryImpl implements CloudRepository {

    private CloudValidateApiDelegate cloudValidateApiDelegate;
    private CloudSprinklersApiDelegate cloudSprinklersApiDelegate;

    public CloudRepositoryImpl(CloudValidateApiDelegate cloudValidateApiDelegate,
                               CloudSprinklersApiDelegate cloudSprinklersApiDelegate) {
        this.cloudValidateApiDelegate = cloudValidateApiDelegate;
        this.cloudSprinklersApiDelegate = cloudSprinklersApiDelegate;
    }

    @Override
    public Completable validateEmail(String email, String deviceName, String mac) {
        return cloudValidateApiDelegate.validateEmail(email, deviceName, mac);
    }

    @Override
    public Single<List<CloudEntry>> cloudEntries(List<Pair<String, String>> emailPasswordPairs) {
        return cloudSprinklersApiDelegate.cloudEntries(emailPasswordPairs);
    }
}
