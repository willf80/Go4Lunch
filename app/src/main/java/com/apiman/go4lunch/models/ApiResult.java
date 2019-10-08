package com.apiman.go4lunch.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ApiResult {
    private String id;
    private String name;
    private String reference;
    private String vicinity;

    @SerializedName("place_id")
    private String placeId;

    @SerializedName("user_ratings_total")
    private double userRatingsTotal;

    private double rating;

    private Geometry geometry;

    private List<Photo> photos;

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

    class Geometry {
        Location location;
    }

    class Location {
        double lat;
        double lng;
    }

    Restaurant toRestaurant() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(id);
        restaurant.setName(name);
        restaurant.setPlaceId(placeId);
        restaurant.setRating(rating);
        restaurant.setReference(reference);
        restaurant.setUserRatingsTotal(userRatingsTotal);

        if(vicinity != null) {
            String  address = vicinity.split(",")[0];
            restaurant.setAddress(address);
        }

        if(geometry != null && geometry.location != null) {
            restaurant.setLatitude(geometry.location.lat);
            restaurant.setLongitude(geometry.location.lng);
        }

        if(photos != null && photos.size() > 0) {
            restaurant.setPhotoReference(photos.get(0).reference);
        }

        return restaurant;
    }
}
