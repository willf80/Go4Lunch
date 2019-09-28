package com.apiman.go4lunch.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Configuration extends RealmObject {
    @PrimaryKey
    private int id = 1;
    private boolean isLocationPermission;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isLocationPermission() {
        return isLocationPermission;
    }

    public void setLocationPermission(boolean locationPermission) {
        isLocationPermission = locationPermission;
    }
}
