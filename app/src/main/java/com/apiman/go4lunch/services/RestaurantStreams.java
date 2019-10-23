package com.apiman.go4lunch.services;

import android.content.Context;

import com.apiman.go4lunch.R;
import com.apiman.go4lunch.helpers.Utils;
import com.apiman.go4lunch.models.ApiDetailsResponse;
import com.apiman.go4lunch.models.ApiDetailsResult;
import com.apiman.go4lunch.models.ApiResponse;
import com.apiman.go4lunch.models.Period;
import com.apiman.go4lunch.models.Photo;
import com.apiman.go4lunch.models.Restaurant;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RestaurantStreams {

    public static Flowable<ApiResponse> getNearbyRestaurantsObservable(Context context, LatLng latLng) {
        Map<String, String> parameters = ApiClientConfig.getNearbyDefaultParameters(context);
        parameters.put("location", String.format(Locale.getDefault(),
                "%s,%s", latLng.latitude, latLng.longitude));

        return ApiClientConfig
                .getHttpClient(context)
                .create(RestaurantService.class)
                .getNearbyRestaurantsObservable(parameters);
    }

    public static Flowable<ApiDetailsResponse> getRestaurantDetailsFlowable(Context context, String placeId) {
        Map<String, String> parameters = ApiClientConfig.getDefaultParameters(context);
        parameters.put("place_id", placeId);
        parameters.put("fields", "name,vicinity,photo,opening_hours,international_phone_number,place_id,website");

        return ApiClientConfig
                .getHttpClient(context)
                .create(RestaurantService.class)
                .getRestaurantDetailsObservable(parameters);
    }

    public static Flowable<Restaurant> getRestaurantDetailsExtractedFlowable(Context context, String placeId) {
        return RestaurantStreams
                .getRestaurantDetailsFlowable(context, placeId)
                .map(response -> fetchRestaurantInfo(context, response.getApiResult(), placeId))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }

    private static Restaurant fetchRestaurantInfo(Context context, ApiDetailsResult detailsResult, String placeId) {
        Restaurant restaurant = applyRestaurantDetails(context, new Restaurant(), detailsResult);

        String vicinity = detailsResult.getVicinity();
        if(vicinity != null) {
            String  address = vicinity.split(",")[0];
            restaurant.setAddress(address);
        }

        List<Photo> photos = detailsResult.getPhotos();
        if(photos != null && photos.size() > 0) {
            restaurant.setPhotoReference(photos.get(0).reference);
        }

        restaurant.setPlaceId(placeId);
        restaurant.setName(detailsResult.getName());
        return restaurant;
    }

    public static Restaurant applyRestaurantDetails(Context context, Restaurant restaurant, ApiDetailsResult detailsResult){
        ApiDetailsResult.OpeningHour openingHour = detailsResult.getOpeningHour();

        if (openingHour != null) {
            List<Period> periodList = openingHour.periods;

            int dayIndex = Utils.getDayOfWeek();
            int currentTime = Utils.getCurrentTime();
            boolean isOpenNow = openingHour.isOpenNow;
            boolean isClosingSoon = Utils.isClosingSoon(periodList, dayIndex, currentTime);

            Period period = Utils.getCurrentPeriod(periodList, dayIndex, currentTime);
            String status = Utils.restaurantStatus(context, isOpenNow, isClosingSoon, period);

            restaurant.setOpenNow(isOpenNow);
            restaurant.setClosingSoon(isClosingSoon);
            restaurant.setStatus(status);
        }

        restaurant.setWebsite(detailsResult.getWebsite());
        restaurant.setPhoneNumber(detailsResult.getPhoneNumber());

        return restaurant;
    }

    private static String getPhotoUrl(Context context, String reference, int width) {
        if(reference == null || reference.isEmpty()) {
            return null;
        }

        String key = context.getString(R.string.place_api_key);
        String photoUrl = context.getString(R.string.place_photo_url);
        return String.format(Locale.getDefault(), photoUrl, width, reference, key);
    }

    public static String getSmallPhotoUrl(Context context, String reference) {
        return getPhotoUrl(context, reference, 100);
    }

    public static String getMediumPhotoUrl(Context context, String reference) {
        return getPhotoUrl(context, reference, 400);
    }

}
