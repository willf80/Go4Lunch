package com.apiman.go4lunch.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ApiDetailsResult {
    @SerializedName("place_id")
    private String placeId;

    @SerializedName("international_phone_number")
    private String phoneNumber;

    @SerializedName("opening_hours")
    private OpeningHour openingHour;

    private String website;
    private String name;
    private String vicinity;

    private List<Photo> photos;

    public String getPlaceId() {
        return placeId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getWebsite() {
        return website;
    }

    public String getName() {
        return name;
    }

    public String getVicinity() {
        return vicinity;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public OpeningHour getOpeningHour() {
        return openingHour;
    }


    public static class OpeningHour {
        @SerializedName("open_now")
        public boolean isOpenNow;

        public List<Period> periods;
    }
}
