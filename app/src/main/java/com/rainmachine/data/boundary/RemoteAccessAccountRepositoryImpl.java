package com.rainmachine.data.boundary;

import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.data.local.database.model.CloudInfo;
import com.rainmachine.domain.boundary.data.RemoteAccessAccountRepository;

public class RemoteAccessAccountRepositoryImpl implements RemoteAccessAccountRepository {

    private DatabaseRepositoryImpl databaseRepository;

    public RemoteAccessAccountRepositoryImpl(DatabaseRepositoryImpl databaseRepository) {
        this.databaseRepository = databaseRepository;
    }

    @Override
    public void saveAccount(String email, String password) {
        CloudInfo cloudInfo = databaseRepository.getCloudInfo(email);
        if (cloudInfo == null) {
            cloudInfo = new CloudInfo();
            cloudInfo.email = email;
            cloudInfo.password = password;
        } else {
            // Update password
            cloudInfo.password = password;
        }
        databaseRepository.saveCloudInfo(cloudInfo);
    }
}
