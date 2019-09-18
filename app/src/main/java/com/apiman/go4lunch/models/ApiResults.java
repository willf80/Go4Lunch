package com.apiman.go4lunch.models;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class ApiResults extends RealmObject {
    private String id;
    private String name;
    private String reference;

    @SerializedName("place_id")
    private String placeId;

    private double rating;

    @SerializedName("user_ratings_total")
    private double userRatingsTotal;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public double getUserRatingsTotal() {
        return userRatingsTotal;
    }

    public void setUserRatingsTotal(double userRatingsTotal) {
        this.userRatingsTotal = userRatingsTotal;
    }
}
