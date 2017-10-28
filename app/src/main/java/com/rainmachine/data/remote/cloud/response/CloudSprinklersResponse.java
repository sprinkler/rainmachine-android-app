package com.rainmachine.data.remote.cloud.response;

import java.util.List;

public class CloudSprinklersResponse {
    public String email;
    public List<CloudSprinklerResponse> sprinklers;
    public int activeCount;
    public int knownCount;
    public int authCount;
}
