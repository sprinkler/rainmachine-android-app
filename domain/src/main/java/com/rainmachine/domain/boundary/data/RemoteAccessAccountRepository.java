package com.rainmachine.domain.boundary.data;

public interface RemoteAccessAccountRepository {

    void saveAccount(String email, String password);
}
