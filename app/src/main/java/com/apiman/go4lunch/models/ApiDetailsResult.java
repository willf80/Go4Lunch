package com.apiman.go4lunch.models;

import com.apiman.go4lunch.services.Utils;
import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.realm.RealmList;

public class ApiDetailsResult {
    @SerializedName("place_id")
    private String placeId;

    @SerializedName("international_phone_number")
    private String phoneNumber;

    @SerializedName("opening_hours")
    private OpeningHour openingHour;

    private String website;

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public OpeningHour getOpeningHour() {
        return openingHour;
    }

    public void setOpeningHour(OpeningHour openingHour) {
        this.openingHour = openingHour;
    }

    public static class OpeningHour {
        @SerializedName("open_now")
        public boolean isOpenNow;

        @SerializedName("weekday_text")
        public List<String> weekDayText;

        public List<Period> periods;
    }

    public static class Period {
        public DayTime open;
        public DayTime close;
    }

    public static class DayTime{
        public int day;

        @SerializedName("time")
        public String timeText;

        public DayTime() {
        }

        public DayTime(int day, String timeText) {
            this.day = day;
            this.timeText = timeText;
        }

        //        @SerializedName("time")
//        public Integer time;
    }

    private int dayIndex() {
        Calendar now = Calendar.getInstance(Locale.FRANCE);

        switch (now.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                return 0;

            case Calendar.TUESDAY:
                return 1;

            case Calendar.WEDNESDAY:
                return 2;

            case Calendar.THURSDAY:
                return 3;

            case Calendar.FRIDAY:
                return 4;

            case Calendar.SUNDAY:
                return 5;

            default:
                return 6;
        }
    }

    public OpenCloseHour getOpenCloseHour() {
        if(openingHour == null || openingHour.periods == null) {
            return null;
        }

        List<Period> periodList = openingHour.periods;

        Calendar now = Calendar.getInstance(Locale.FRANCE);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HHmm", Locale.getDefault());
        String timeText = simpleDateFormat.format(now.getTime());

        int time = (now.get(Calendar.HOUR_OF_DAY) * 100) + now.get(Calendar.MINUTE);
        int dayOfWeek = dayIndex();

        OpenCloseHour openCloseHour = new OpenCloseHour();

//        Period period = null;
        String weekdayText = openingHour.weekDayText.get(dayOfWeek);
        if (weekdayText != null) {
            String text = weekdayText;

            String[] periods = weekdayText.split(",");
            if(periods.length > 1){
                text = periods[periods.length -1];
                text = text.trim();
            }

            text = Utils.getHour(text);

            openCloseHour.setTimeText(text);
        }

//        for (Period period : openingHour.periods) {
//            OpenCloseHour closeHour = new OpenCloseHour();
//            DayTime closeDayTime = period.close;
//            if(closeDayTime != null){
//                closeHour.setPlaceId(placeId);
//                closeHour.setDay(closeDayTime.day);
//                closeHour.setTimeText(closeDayTime.time);
//                closeHour.setClose(true);
//
//                openCloseHours.add(closeHour);
//            }
//
//            OpenCloseHour openHour = new OpenCloseHour();
//            DayTime openDayTime = period.open;
//            if(openDayTime != null){
//                openHour.setPlaceId(placeId);
//                openHour.setDay(openDayTime.day);
//                openHour.setTimeText(openDayTime.time);
//                openHour.setClose(false);
//
//                openCloseHours.add(openHour);
//            }
//        }

        return openCloseHour;
    }
}
