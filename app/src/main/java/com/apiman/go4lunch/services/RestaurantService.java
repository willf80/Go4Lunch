package com.apiman.go4lunch.services;

import com.apiman.go4lunch.models.ApiDetailsResponse;
import com.apiman.go4lunch.models.ApiResponse;

import java.util.Map;

import io.reactivex.Flowable;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface RestaurantService {

    @GET("maps/api/place/nearbysearch/json")
    Flowable<ApiResponse> getNearbyRestaurantsObservable(@QueryMap Map<String, String> parameters);

    @GET("maps/api/place/details/json")
    Flowable<ApiDetailsResponse> getRestaurantDetailsObservable(@QueryMap Map<String, String> parameters);

}
