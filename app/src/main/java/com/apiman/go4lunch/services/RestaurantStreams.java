package com.apiman.go4lunch.services;

import android.content.Context;

import com.apiman.go4lunch.R;
import com.apiman.go4lunch.models.ApiDetailsResponse;
import com.apiman.go4lunch.models.ApiDetailsResult;
import com.apiman.go4lunch.models.ApiResponse;
import com.apiman.go4lunch.models.Photo;
import com.apiman.go4lunch.models.Restaurant;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.Flowable;

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
        return getRestaurantDetailsFlowable(context, placeId,
                "name,vicinity,photo,opening_hours,international_phone_number,place_id,website");
    }

    private static Flowable<ApiDetailsResponse> getRestaurantBasicDetailsFlowable(Context context, String placeId) {
        return getRestaurantDetailsFlowable(context, placeId,
                "name,vicinity,photo,international_phone_number,place_id,website");
    }

    private static Flowable<ApiDetailsResponse> getRestaurantDetailsFlowable(Context context, String placeId, String fields) {
        Map<String, String> parameters = ApiClientConfig.getDefaultParameters(context);
        parameters.put("place_id", placeId);
        parameters.put("fields", fields);

        return ApiClientConfig
                .getHttpClient(context)
                .create(RestaurantService.class)
                .getRestaurantDetailsObservable(parameters);
    }

    public static Flowable<Restaurant> getRestaurantDetailsExtractedFlowable(Context context, String placeId) {
        return RestaurantStreams
                .getRestaurantBasicDetailsFlowable(context, placeId)
                .map(response -> fetchRestaurantInfo(response.getApiResult(), placeId));
    }

    private static Restaurant fetchRestaurantInfo(ApiDetailsResult detailsResult, String placeId) {
        Restaurant restaurant = new Restaurant();

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
