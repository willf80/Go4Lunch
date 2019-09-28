package com.apiman.go4lunch.ui;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.apiman.go4lunch.models.ApiDetailsResult;
import com.apiman.go4lunch.models.ApiResponse;
import com.apiman.go4lunch.models.Restaurant;
import com.apiman.go4lunch.services.RestaurantStreams;
import com.apiman.go4lunch.services.Utils;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
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

                        getRestaurantsDetails(context, restaurants, latLng);
                    }

                    @Override
                    public void onFailure(@NonNull  Call<ApiResponse> call, @NonNull  Throwable t) {

                    }
                });
    }

    private Completable restaurantDetailsCompletable(final Context context, Restaurant restaurant, final LatLng currentLocation) {
        return Completable.fromObservable(
                RestaurantStreams
                .getRestaurantDetailsObservable(context, restaurant.getPlaceId())
                .map(detailsResponse -> {
                    ApiDetailsResult detailsResult = detailsResponse.getApiResult();
                    if(detailsResult == null)
                        return Observable.error(new Exception("Details info not found"));

                    return updatedRestaurantInfo(detailsResult, currentLocation);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
            )
            .onErrorComplete()
            .timeout(10, TimeUnit.SECONDS);
    }

    private void getRestaurantsDetails(final Context context, List<Restaurant> restaurants, final LatLng currentLocation) {
        // Create iterable observable
        Observable.fromIterable(restaurants)
            .flatMapCompletable(restaurant -> restaurantDetailsCompletable(context, restaurant, currentLocation))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally(this::updateRestaurantsInfo)// update restaurant list
            .subscribe();
    }

    private void updateRestaurantsInfo() {
        mRestaurantList.setValue(
                mRealm.where(Restaurant.class)
                        .sort("distance")
                        .findAll());
    }

    private Restaurant updatedRestaurantInfo(ApiDetailsResult detailsResult, LatLng currentLocation) {
        try (Realm realm = Realm.getDefaultInstance()) {

            Restaurant restaurant = realm.where(Restaurant.class)
                    .equalTo("placeId", detailsResult.getPlaceId())
                    .findFirst();

            if(restaurant == null) return null;

            // Calculate distance
            int distance = Utils.distanceInMeters(
                    currentLocation.latitude, currentLocation.longitude,
                    restaurant.getLatitude(), restaurant.getLongitude()
            );

            // Update restaurant info
            realm.beginTransaction();

            // Set restaurant info
            if (detailsResult.getOpeningHour() != null) {
                restaurant.setOpenNow(detailsResult.getOpeningHour().isOpenNow);
            }

            if(detailsResult.getOpenCloseHour() != null) {
                restaurant.setTimeText(detailsResult.getOpenCloseHour().getTimeText());
            }

            restaurant.setWebsite(detailsResult.getWebsite());
            restaurant.setPhoneNumber(detailsResult.getPhoneNumber());
            restaurant.setDistance(distance);

//            realm.insertOrUpdate(detailsResult.getOpenCloseHourList());

            realm.commitTransaction();
            Log.e("Base", restaurant.getPlaceId() + " : " + restaurant.isOpenNow());
            return restaurant;
        }
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
