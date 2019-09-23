package com.apiman.go4lunch.models;

import com.google.gson.annotations.SerializedName;

public class ApiDetailsResponse {
    private String status;

    @SerializedName("result")
    private ApiDetailsResult apiResult;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ApiDetailsResult getApiResult() {
        return apiResult;
    }

    public void setApiResult(ApiDetailsResult apiResult) {
        this.apiResult = apiResult;
    }
}

