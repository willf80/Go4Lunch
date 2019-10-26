package com.apiman.go4lunch.models;

import java.util.Date;

public class Workmate {
    public String uuid;
    public String displayName;
    public String email;
    public String photo;
    public Date lastConnectionDate;

    public Workmate() {
    }

    public Workmate(String uuid, String displayName, String email, String photo, Date lastConnectionDate) {
        this.uuid = uuid;
        this.displayName = displayName;
        this.email = email;
        this.photo = photo;
        this.lastConnectionDate = lastConnectionDate;
    }
}
