package com.apiman.go4lunch.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class MyLocation extends RealmObject {
    @PrimaryKey
    int id = 1;
    double lat;
    double lng;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
