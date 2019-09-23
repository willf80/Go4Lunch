package com.apiman.go4lunch.ui;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.apiman.go4lunch.models.ApiDetailsResponse;
import com.apiman.go4lunch.models.ApiDetailsResult;
import com.apiman.go4lunch.models.ApiResponse;
import com.apiman.go4lunch.models.Restaurant;
import com.apiman.go4lunch.services.RestaurantStreams;
import com.apiman.go4lunch.services.Utils;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BaseViewModel extends ViewModel {

    // Location permission state
    private MutableLiveData<Boolean> mLocationPermission;

    // Last location
    private MutableLiveData<LatLng> mLastKnowLocation;

    // List of restaurants
    private MutableLiveData<List<Restaurant>> mRestaurantList;

    private Realm mRealm;

    public BaseViewModel() {
        mRealm = Realm.getDefaultInstance();

        mLocationPermission = new MutableLiveData<>();
        mLocationPermission.setValue(false);

        mLastKnowLocation = new MutableLiveData<>();
        mLastKnowLocation.setValue(null);

        restaurantChangeListener();
    }

    private void restaurantChangeListener() {
        mRealm.addChangeListener(realm -> {
            if(mRestaurantList == null) return;
            mRestaurantList.setValue(realm.where(Restaurant.class).findAll());
        });
    }

    //---- Start Restaurants
    public LiveData<List<Restaurant>> getRestaurantList(Context context, LatLng latLng) {
        if(mRestaurantList == null) {
            mRestaurantList = new MutableLiveData<>();
            loadRestaurants(context, latLng);
        }
        return mRestaurantList;
    }

    private void loadRestaurants(final Context context, LatLng latLng) {
        if(latLng == null) return;
        RestaurantStreams.getNearbyRestaurants(context, latLng)
                .enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                        if(!response.isSuccessful()) return;

                        ApiResponse apiResponse = response.body();
                        if(apiResponse == null) return;

                        List<Restaurant> restaurants = apiResponse.getRestaurants();
                        saveRestaurants(restaurants);

                        for (Restaurant restaurant : restaurants) {
                            getRestaurantsDetails(context, restaurant, latLng);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull  Call<ApiResponse> call, @NonNull  Throwable t) {

                    }
                });
    }

    private void getRestaurantsDetails(final Context context, Restaurant restaurant, final LatLng currentLocation) {
        if(restaurant == null) return;

        RestaurantStreams.getRestaurantDetails(context, restaurant.getPlaceId())
                .enqueue(new Callback<ApiDetailsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiDetailsResponse> call, @NonNull Response<ApiDetailsResponse> response) {
                        if(!response.isSuccessful()) return;

                        ApiDetailsResponse detailsResponse = response.body();
                        if(detailsResponse == null) return;

                        ApiDetailsResult detailsResult = detailsResponse.getApiResult();
                        if(detailsResult == null) return;

                        updatedRestaurantInfo(restaurant, detailsResult, currentLocation);
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiDetailsResponse> call, @NonNull Throwable t) {

                    }
                });
    }

    private void updatedRestaurantInfo(Restaurant restaurant, ApiDetailsResult detailsResult, LatLng currentLocation) {
        restaurant.setPlaceId(detailsResult.getPlaceId());

        int distance = Utils.distanceInMeters(
                currentLocation.latitude, currentLocation.longitude,
                restaurant.getLatitude(), restaurant.getLongitude()
        );

        restaurant.setDistance(distance);

        mRealm.beginTransaction();
        mRealm.copyToRealmOrUpdate(restaurant);
        mRealm.commitTransaction();
    }
    //---- END

    private void saveRestaurants(List<Restaurant> restaurants) {
        mRealm.executeTransaction(realm -> {
            realm.delete(Restaurant.class);
            realm.copyToRealmOrUpdate(restaurants);
        });
    }

    public LiveData<LatLng> getLastKnowLocation() {
        return mLastKnowLocation;
    }

    void setLastKnowLocation(LatLng lastKnowLocation) {
        mLastKnowLocation.setValue(lastKnowLocation);
    }

    public LiveData<Boolean> getLocationPermissionState() {
        return mLocationPermission;
    }

    void setLocationPermissionState(boolean state){
        mLocationPermission.setValue(state);
    }

}
