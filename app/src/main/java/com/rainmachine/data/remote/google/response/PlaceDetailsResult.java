package com.rainmachine.data.remote.google.response;

import java.util.List;

public class PlaceDetailsResult {
    public List<ComponentsResponse> address_components;
    public String formatted_address;
    public PlaceGeometry geometry;
}
