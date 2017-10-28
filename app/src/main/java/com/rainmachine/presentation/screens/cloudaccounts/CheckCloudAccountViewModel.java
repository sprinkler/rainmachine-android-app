package com.rainmachine.presentation.screens.cloudaccounts;

import com.rainmachine.data.local.database.model.CloudInfo;
import com.rainmachine.domain.model.CheckCloudAccountStatus;

class CheckCloudAccountViewModel {

    public CheckCloudAccountStatus status;
    public CloudInfo cloudInfo;

    public CheckCloudAccountViewModel(CheckCloudAccountStatus status, CloudInfo cloudInfo) {
        this.status = status;
        this.cloudInfo = cloudInfo;
    }
}
