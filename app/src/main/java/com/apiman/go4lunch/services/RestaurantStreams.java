package com.apiman.go4lunch.services;

import android.content.Context;

import com.apiman.go4lunch.models.ApiResponse;
import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;
import java.util.Map;

import retrofit2.Call;

public class RestaurantStreams {

    public static Call<ApiResponse> getNearbyRestaurants(Context context, LatLng latLng) {
        Map<String, String> parameters = ApiClientConfig.getDefaultParameters(context);
        parameters.put("location", String.format(Locale.getDefault(),
                "%s,%s", latLng.latitude, latLng.longitude));

        return ApiClientConfig
                .getHttpClient(context)
                .create(RestaurantService.class)
                .getNearbyRestaurants(parameters);
    }

}
