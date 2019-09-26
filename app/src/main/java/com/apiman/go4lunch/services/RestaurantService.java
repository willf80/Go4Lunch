package com.apiman.go4lunch.services;

import com.apiman.go4lunch.models.ApiDetailsResponse;
import com.apiman.go4lunch.models.ApiResponse;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface RestaurantService {

    @GET("maps/api/place/nearbysearch/json")
    Call<ApiResponse> getNearbyRestaurants(@QueryMap Map<String, String> parameters);

    @GET("maps/api/place/details/json")
    Observable<ApiDetailsResponse> getRestaurantDetailsObservable(@QueryMap Map<String, String> parameters);

}
