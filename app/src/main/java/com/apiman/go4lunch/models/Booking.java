package com.apiman.go4lunch.models;

public class Booking {
    public String placeId;
    public String userRef;
    public long timestamps;

    public Booking() {
    }

    public Booking(String placeId, String userRef, long timestamps) {
        this.placeId = placeId;
        this.userRef = userRef;
        this.timestamps = timestamps;
    }
}
