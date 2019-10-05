package com.apiman.go4lunch.services;

import androidx.annotation.Nullable;

import com.apiman.go4lunch.models.DayTime;
import com.apiman.go4lunch.models.Period;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.reactivex.Flowable;

/**
 * Source : http://villemin.gerard.free.fr/aGeograp/Distance.htm
 */
public class Utils {
    private static final int earthRadius = 6_371;

    public static int distanceInMeters(double lat1, double lon1, double lat2, double lon2) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }

        double theta = lon1 - lon2;
        double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
        dist = Math.acos(dist) * earthRadius;

        return (int)Math.round(dist * 1000);
    }

    public static int getDayOfWeek() {
        return  Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
    }

    static String today() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return simpleDateFormat.format(Calendar.getInstance().getTime());
    }

    public static int getCurrentTime() {
        Calendar now = Calendar.getInstance();

        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minutes = now.get(Calendar.MINUTE);

        return (hour * 100) + minutes;
    }


    private static boolean isSamePeriod(Period period, int dayIndex) {
        DayTime open = period.open;
        DayTime close = period.close;

        boolean samePeriod = false;

        if(open != null) {
            samePeriod = open.day == dayIndex;
        }

        if(!samePeriod && close != null) {
            samePeriod = close.day == dayIndex;
        }

        return samePeriod;
    }

    private static boolean isValidInterval(Period period, int dayIndex, int currentHour){
        DayTime open = period.open;
        DayTime close = period.close;

        if(open == null || close == null) {
            return false;
        }

        try {
            int openTime = Integer.parseInt(open.timeText);
            int closeTime = Integer.parseInt(close.timeText);

            if(open.day != close.day && close.day == dayIndex) {
                if(currentHour >= 0 && currentHour <= closeTime) {
                    return true;
                }
            }

            return currentHour >= openTime && currentHour <= closeTime;
        }catch (Exception e) {
            return false;
        }
    }

    private static Period transformPeriod(Period period, int dayIndex){
        DayTime open = period.open;
        DayTime close = period.close;

        if(open == null || close == null){
            return period;
        }

        if(open.day != close.day) {
            int closeTime = Integer.parseInt(close.timeText);

            if(open.day == dayIndex) {
                closeTime += 2400;
            }

            close.timeText = closeTime + "";
        }

        return period;
    }

    private static boolean isTimeGreaterThanClosingTime(Period period, int currentHour){
        DayTime close = period.close;
        if(close == null) {
            return false;
        }

        String timeText = close.timeText;
        try {
            int time = Integer.parseInt(timeText);
            int currentHourExtra = currentHour + 100; // plus 60 min

            if(currentHourExtra > time) {
                return true;
            }
        }catch (Exception e) {
            return false;
        }

        return false;
    }

    private static List<Period> copyPeriodList(List<Period> periods) {
        List<Period> copyList = new ArrayList<>();
        for (Period period : periods) {
            if(period == null) continue;
            copyList.add(period.copy());
        }

        return copyList;
    }

    private static Iterable<Period> getPeriodOfDayIterableList(List<Period> periods, int dayIndex){
        List<Period> copyList = copyPeriodList(periods);
        return Flowable
                .fromIterable(copyList)
                .filter(period -> isSamePeriod(period, dayIndex))
                .map(period -> transformPeriod(period, dayIndex))
                .blockingIterable();
    }

    public static Period getCurrentPeriod(List<Period> periods, int dayIndex, int currentHour) {
        return Flowable
                .fromIterable(getPeriodOfDayIterableList(periods, dayIndex))
                .filter(period -> isValidInterval(period, dayIndex, currentHour))
                .blockingLast(null);
    }

    public static boolean isClosingSoon(List<Period> periods, int dayIndex, int currentHour) {
        return Flowable
                .fromIterable(getPeriodOfDayIterableList(periods, dayIndex))
                .filter(period -> isValidInterval(period, dayIndex, currentHour))
                .map(period -> isTimeGreaterThanClosingTime(period, currentHour))
                .blockingFirst(false);
    }

    public static String restaurantStatus(boolean isOpenNow, boolean isClosingSoon, @Nullable Period period){
        return restaurantStatus(isOpenNow, isClosingSoon, period, Locale.getDefault());
    }

    public static String restaurantStatus(
            boolean isOpenNow, boolean isClosingSoon, @Nullable Period period, Locale locale){
        if(!isOpenNow){
            return "Closed";
        }

        if(isClosingSoon) {
            return "Closing soon";
        }

        if((period == null) || (period.close == null)) {
            return "Open 24/7";
        }

        return "Open until " + period.close.getTime(locale);
    }

    public static String shortName(String fullName) {
        if(fullName == null) return null;

        fullName = fullName.trim();
        int index = fullName.indexOf(' ');
        return fullName.substring(0, index).trim();
    }
}
