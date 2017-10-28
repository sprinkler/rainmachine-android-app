package com.rainmachine.data.remote.sprinkler.v3.response;

import com.google.gson.annotations.SerializedName;

public class UpdateResponse3 extends BaseResponse3 {
    public boolean update;
    @SerializedName("current_version")
    public String currentVersion;
    @SerializedName("new_version")
    public String newVersion;
    @SerializedName("last_update_check")
    public long lastUpdateCheck;
    @SerializedName("update_status")
    public int updateStatus;
}
