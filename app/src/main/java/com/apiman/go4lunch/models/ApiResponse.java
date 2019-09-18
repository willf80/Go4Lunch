package com.apiman.go4lunch.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ApiResponse {
    private String status;

    @SerializedName("results")
    private List<ApiResults> apiResults;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ApiResults> getApiResults() {
        return apiResults;
    }

    public void setApiResults(List<ApiResults> apiResults) {
        this.apiResults = apiResults;
    }
}

