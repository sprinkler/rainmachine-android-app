package com.rainmachine.data.remote.google.response;

import java.util.List;

public class GeoCodingAddressResponse {
    public List<ComponentsResponse> address_components;
    public String formatted_address;
    public GeometryResponse geometry;
    public List<String> types;
}
