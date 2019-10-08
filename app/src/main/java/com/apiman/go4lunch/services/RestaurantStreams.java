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

import io.reactivex.Observable;
import retrofit2.Call;

public class RestaurantStreams {

    public static Call<ApiResponse> getNearbyRestaurants(Context context, LatLng latLng) {
        Map<String, String> parameters = ApiClientConfig.getNearbyDefaultParameters(context);
        parameters.put("location", String.format(Locale.getDefault(),
                "%s,%s", latLng.latitude, latLng.longitude));

        return ApiClientConfig
                .getHttpClient(context)
                .create(RestaurantService.class)
                .getNearbyRestaurants(parameters);
    }


    public static Observable<ApiDetailsResponse> getRestaurantDetailsObservable(Context context, String placeId) {
        return getRestaurantDetailsObservable(context, placeId,
                "name,vicinity,opening_hours,international_phone_number,place_id,website");
    }

    public static Observable<ApiDetailsResponse> getRestaurantBasicDetailsObservable(Context context, String placeId) {
        return getRestaurantDetailsObservable(context, placeId,
                "name,vicinity,international_phone_number,place_id,website");
    }

    private static Observable<ApiDetailsResponse> getRestaurantDetailsObservable(Context context, String placeId, String fields) {
        Map<String, String> parameters = ApiClientConfig.getDefaultParameters(context);
        parameters.put("place_id", placeId);
        parameters.put("fields", fields);

        return ApiClientConfig
                .getHttpClient(context)
                .create(RestaurantService.class)
                .getRestaurantDetailsObservable(parameters);
    }

    public static Observable<Restaurant> getRestaurantDetailsExtractedObservable(Context context, String placeId) {
        return RestaurantStreams
                .getRestaurantBasicDetailsObservable(context, placeId)
                .map(response -> fetchRestaurant(response.getApiResult(), placeId));
    }

    private static Restaurant fetchRestaurant(ApiDetailsResult detailsResult, String placeId) {
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
