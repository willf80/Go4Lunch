package com.apiman.go4lunch.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ApiResult {
    private String id;
    private String name;
    private String vicinity;

    @SerializedName("place_id")
    private String placeId;

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

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getVicinity() {
        return vicinity;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    Restaurant toRestaurant() {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setPlaceId(placeId);

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
