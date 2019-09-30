package com.apiman.go4lunch.ui;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.apiman.go4lunch.models.ApiDetailsResult;
import com.apiman.go4lunch.models.ApiResponse;
import com.apiman.go4lunch.models.Period;
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

    private void applyRestaurantDetails(Restaurant restaurant, ApiDetailsResult detailsResult, int distance){
        Realm realm = Realm.getDefaultInstance();
        ApiDetailsResult.OpeningHour openingHour = detailsResult.getOpeningHour();

        realm.beginTransaction();
        if (openingHour != null) {
            List<Period> periodList = openingHour.periods;

            int dayIndex = Utils.getDayOfWeek();
            int currentTime = Utils.getCurrentTime();
            boolean isOpenNow = openingHour.isOpenNow;
            boolean isClosingSoon = Utils.isClosingSoon(periodList, dayIndex, currentTime);

            Period period = Utils.getCurrentPeriod(periodList, dayIndex, currentTime);
            String status = Utils.restaurantStatus(isOpenNow, isClosingSoon, period);

            restaurant.setOpenNow(isOpenNow);
            restaurant.setClosingSoon(isClosingSoon);
            restaurant.setStatus(status);
        }

        restaurant.setWebsite(detailsResult.getWebsite());
        restaurant.setPhoneNumber(detailsResult.getPhoneNumber());
        restaurant.setDistance(distance);

        realm.commitTransaction();
    }

    private Restaurant getRestaurantByPlaceId(String placeId) {
        return Realm.getDefaultInstance().where(Restaurant.class)
                .equalTo("placeId", placeId)
                .findFirst();
    }

    private Restaurant updatedRestaurantInfo(ApiDetailsResult detailsResult, LatLng currentLocation) {
        Restaurant restaurant = getRestaurantByPlaceId(detailsResult.getPlaceId());

        if(restaurant == null) return null;

        // Calculate distance
        int distance = Utils.distanceInMeters(
                currentLocation.latitude, currentLocation.longitude,
                restaurant.getLatitude(), restaurant.getLongitude()
        );

        // Update restaurant info
        applyRestaurantDetails(restaurant, detailsResult, distance);

        return restaurant;
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
