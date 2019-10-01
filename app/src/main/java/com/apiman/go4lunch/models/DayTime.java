package com.apiman.go4lunch.models;

import com.google.gson.annotations.SerializedName;

import java.util.Locale;

public class DayTime {
    public int day;

    @SerializedName("time")
    public String timeText;

    public DayTime(int day, String timeText) {
        this.day = day;
        this.timeText = timeText;
    }

    private int realTime() {
        int time = Integer.parseInt(timeText);
        if(time >= 2400){
            time -= 2400;
        }

        return time;
    }

    private int getHours() {
        int time = realTime();
        return time / 100;
    }

    private int getMinutes() {
        int time = realTime();
        return time % 100;
    }

    public String getTime(Locale locale) {
        int hour24 = getHours();
        int minutes = getMinutes();

        if(locale.getLanguage().equals(Locale.FRENCH.getLanguage())){
            return hour24 + "h" + addLeftZeroInMinutes(minutes);
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

    DayTime copy(){
        return new DayTime(day, timeText);
    }
}
