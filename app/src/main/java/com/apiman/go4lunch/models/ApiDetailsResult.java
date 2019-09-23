package com.apiman.go4lunch.models;

import com.google.gson.annotations.SerializedName;

public class ApiDetailsResult {
    @SerializedName("place_id")
    private String placeId;

    @SerializedName("international_phone_number")
    private String phoneNumber;

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
