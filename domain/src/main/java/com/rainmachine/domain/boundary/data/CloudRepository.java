package com.rainmachine.domain.boundary.data;

import com.rainmachine.domain.model.CloudEntry;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import kotlin.Pair;

public interface CloudRepository {

    Completable validateEmail(String email, String deviceName, String mac);

    Single<List<CloudEntry>> cloudEntries(List<Pair<String, String>> emailPasswordPairs);
}
