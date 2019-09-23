package com.apiman.go4lunch.services;

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
}
