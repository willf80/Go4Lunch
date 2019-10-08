package com.apiman.go4lunch.models;

import java.util.Locale;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Restaurant extends RealmObject {
    @PrimaryKey
    private String id;
    private String name;
    private String reference;
    private String placeId;
    private String address;
    private String website;
    private String phoneNumber;
    private String status;
    private String photoReference;
    private double latitude;
    private double longitude;
    private double rating;
    private double userRatingsTotal;
    private boolean isBook;
    private boolean isClosingSoon;
    private boolean openNow;
    private int distance;
    private int totalWorkmates = 0;

    public String getDistanceWithSuffix() {
        String suffix = "m";
        if(distance > 1000) {
            suffix = "km";
            distance = distance / 1000;
        }

        return String.format(Locale.getDefault(), "%d%s", distance, suffix );
    }

    //---------- Getters and setters -------------
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

    public boolean isBook() {
        return isBook;
    }

    public void setBook(boolean book) {
        isBook = book;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isOpenNow() {
        return openNow;
    }

    public void setOpenNow(boolean openNow) {
        this.openNow = openNow;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isClosingSoon() {
        return isClosingSoon;
    }

    public void setClosingSoon(boolean closingSoon) {
        isClosingSoon = closingSoon;
    }

    public int getTotalWorkmates() {
        return totalWorkmates;
    }

    public void setTotalWorkmates(int totalWorkmates) {
        this.totalWorkmates = totalWorkmates;
    }

    public String getPhotoReference() {
        return photoReference;
    }

    public void setPhotoReference(String photoReference) {
        this.photoReference = photoReference;
    }
}
