package com.apiman.go4lunch.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ApiResponse {
    private String status;

    @SerializedName("results")
    private List<ApiResult> apiResults;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ApiResult> getApiResults() {
        return apiResults;
    }

    public void setApiResults(List<ApiResult> apiResults) {
        this.apiResults = apiResults;
    }

    public List<Restaurant> getRestaurants() {
        List<Restaurant> restaurants = new ArrayList<>();
        for (ApiResult results :
                apiResults) {
            restaurants.add(results.toRestaurant());
        }

        return restaurants;
    }
}

