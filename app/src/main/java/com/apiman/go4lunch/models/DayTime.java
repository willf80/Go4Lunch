package com.apiman.go4lunch.models;

import com.google.gson.annotations.SerializedName;

import java.util.Locale;

public class DayTime {
    public int day;

    @SerializedName("time")
    public String timeText;

    public DayTime() {
    }

    public DayTime(int day, String timeText) {
        this.day = day;
        this.timeText = timeText;
    }

    private int getHours() {
        int time = Integer.parseInt(timeText);
        return time / 100;
    }

    private int getMinutes() {
        int time = Integer.parseInt(timeText);
        return time % 100;
    }

    public String getTime(Locale locale) {
        int hour24 = getHours();
        int minutes = getMinutes();

        if(locale.getLanguage().equals(new Locale("fr").getLanguage())){
            return hour24 + "h" + minutes;
        }

        if(hour24 > 12) {
            return (hour24 % 12) + "." + addLeftZeroInMinutes(minutes) + "pm";
        }

        return hour24 + "." + addLeftZeroInMinutes(minutes) + "am";
    }

    private String addLeftZeroInMinutes(int minutes) {
        if(minutes < 10) {
            return "0" + minutes;
        }

        return minutes + "";
    }

    public DayTime copy(){
        return new DayTime(day, timeText);
    }
}
