package com.apiman.go4lunch.models;

public class Period {
    public DayTime open;
    public DayTime close;

    // Copy the object with new reference
    public Period copy() {
        Period newPeriod = new Period();
        if(open != null){
            newPeriod.open = open.copy();
        }

        if(close != null){
            newPeriod.close = close.copy();
        }

        return newPeriod;
    }
}
