package com.apiman.go4lunch.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.apiman.go4lunch.models.ApiDetailsResult;
import com.apiman.go4lunch.models.ApiResponse;
import com.apiman.go4lunch.models.Period;
import com.apiman.go4lunch.models.Restaurant;
import com.apiman.go4lunch.services.FireStoreUtils;
import com.apiman.go4lunch.services.RestaurantStreams;
import com.apiman.go4lunch.services.Utils;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;

public class BaseViewModel extends ViewModel {
    private static final String FIELD_PLACE_ID = "placeId";
    private static final String FIELD_TOTAL_WORKMATES = "totalWorkmates";
    private static final String FIELD_IS_BOOK= "isBook";

    // Location permission state
    private MutableLiveData<Boolean> mLocationPermission;

    // Last location
    private MutableLiveData<LatLng> mLastKnowLocation;

    // List of restaurants
    private MutableLiveData<List<Restaurant>> mRestaurantsLiveData;

    private Realm mRealm;
    private CollectionReference todayBooksRef;
    private LatLng lastLatLng;

    public BaseViewModel() {
        mRealm = Realm.getDefaultInstance();

        mLocationPermission = new MutableLiveData<>();
        mLocationPermission.setValue(false);

        mLastKnowLocation = new MutableLiveData<>();
        mRestaurantsLiveData = new MutableLiveData<>();

        todayBooksRef = FireStoreUtils.getTodayBookingCollection();
    }

    //---- Start Restaurants
    public LiveData<List<Restaurant>> getRestaurantList(Context context, LatLng latLng) {
        if(lastLatLng == null) {
            lastLatLng = latLng;
            loadRestaurants(context, latLng);
        }
        return mRestaurantsLiveData;
    }

    public void refreshData(Context context) {
        loadRestaurants(context, lastLatLng);
    }

    private void loadRestaurants(final Context context, LatLng latLng) {
        if(latLng == null) return;

        Disposable disposable = RestaurantStreams.getNearbyRestaurantsObservable(context, latLng)
                .map(ApiResponse::getRestaurants)
                .flatMapIterable(restaurants -> restaurants)
                .flatMap(restaurant -> getRestaurantItemDetails(context, restaurant, latLng))//Get restaurant details
                .toList()
//                .reduce(new ArrayList<Restaurant>(), (restaurants, restaurant) -> {
//                    ApiDetailsResponse response = RestaurantStreams
//                            .getRestaurantDetailsObservable(context, restaurant.getPlaceId())
////                            .subscribeOn(Schedulers.io())
////                            .observeOn(AndroidSchedulers.mainThread())
//                            .blockingSingle(new ApiDetailsResponse());
//
//                    Restaurant newRestaurant = restaurant;
//                    ApiDetailsResult result = response.getApiResult();
//                    if(result == null) {
//                        restaurants.add(newRestaurant);
//                        return restaurants;
//                    }
//
//                    newRestaurant = updatedRestaurantInfo(restaurant, result, latLng);
//                    restaurants.add(newRestaurant);
//
//                    return restaurants;
//                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(apiResponse -> {
//                    getTodayBooking();
                    mRestaurantsLiveData.setValue(apiResponse);
                });

//        RestaurantStreams.getNearbyRestaurants(context, latLng)
//            .enqueue(new Callback<ApiResponse>() {
//                @Override
//                public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
//                    if(!response.isSuccessful()) return;
//
//                    ApiResponse apiResponse = response.body();
//                    if(apiResponse == null) return;
//
//                    List<Restaurant> restaurants = apiResponse.getRestaurants();
//                    saveRestaurants(restaurants);
//
//                    getRestaurantsDetails(context, restaurants, latLng);
//                }
//
//                @Override
//                public void onFailure(@NonNull  Call<ApiResponse> call, @NonNull  Throwable t) {
//
//                }
//            });
    }

    private Observable<Restaurant> getRestaurantItemDetails(Context context, Restaurant restaurant, LatLng latLng) {
        return RestaurantStreams
                .getRestaurantDetailsObservable(context, restaurant.getPlaceId())
                .map(response -> {
                    ApiDetailsResult result = response.getApiResult();
                    if(result == null) {
                        return restaurant;
                    }

                    return updatedRestaurantInfo(restaurant, result, latLng);
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
            .doOnError(throwable -> {})
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally(this::getTodayBooking)// Get Cloud FireStore booking places
            .subscribe();
    }

    // Get Cloud FireStore booking places
    private void getTodayBooking() {
        todayBooksRef.get()
            .addOnSuccessListener(query -> {
                setAsBooking(query);
                updateRestaurantsInfo();
            })
            .addOnFailureListener(e -> updateRestaurantsInfo());
    }

    private void resetBookingAndTotalWorkmatesValues(Realm realm) {
        realm.beginTransaction();
        realm.where(Restaurant.class)
                .greaterThan(FIELD_TOTAL_WORKMATES, 0)
                .findAll()
                .setInt(FIELD_TOTAL_WORKMATES, 0);

        realm.where(Restaurant.class)
                .equalTo(FIELD_IS_BOOK, true)
                .findAll()
                .setBoolean(FIELD_IS_BOOK, false);
        realm.commitTransaction();
    }

    private void setAsBooking(QuerySnapshot query) {
        Realm realm = Realm.getDefaultInstance();

        // Reset all booking and workmates
        resetBookingAndTotalWorkmatesValues(realm);

        realm.beginTransaction();
        HashMap<String, Integer> map = new HashMap<>();
        for (DocumentSnapshot doc : query.getDocuments()) {
            String placeId = doc.get(FIELD_PLACE_ID, String.class);
            if(placeId == null) continue;

            Restaurant restaurant = getRestaurantByPlaceId(placeId);
            if(restaurant == null) continue;

            if(map.containsKey(placeId)){
                Integer current = map.get(placeId);
                if(current == null) {
                    current = 0;
                }

                map.put(placeId,  current + 1);
            }else {
                map.put(placeId, 1);
            }

            restaurant.setBook(true);

        }
        realm.commitTransaction();

        realm.beginTransaction();
        for (HashMap.Entry<String, Integer> entry: map.entrySet()){
            Restaurant restaurant = getRestaurantByPlaceId(entry.getKey());
            if(restaurant == null) continue;
            restaurant.setTotalWorkmates(entry.getValue());
        }
        realm.commitTransaction();
    }

    private void updateRestaurantsInfo() {
        mRestaurantsLiveData.setValue(
                mRealm.where(Restaurant.class)
                        .sort("distance")
                        .findAll());
    }

    private void applyRestaurantDetailsWithRealm(Restaurant restaurant, ApiDetailsResult detailsResult, int distance){
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

    private Restaurant applyRestaurantDetails(Restaurant restaurant, ApiDetailsResult detailsResult, int distance){
        ApiDetailsResult.OpeningHour openingHour = detailsResult.getOpeningHour();

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

        return restaurant;
    }

    private Restaurant getRestaurantByPlaceId(String placeId) {
        return Realm.getDefaultInstance().where(Restaurant.class)
                .equalTo(FIELD_PLACE_ID, placeId)
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
        applyRestaurantDetailsWithRealm(restaurant, detailsResult, distance);

        return restaurant;
    }

    private Restaurant updatedRestaurantInfo(Restaurant restaurant, ApiDetailsResult detailsResult, LatLng currentLocation) {
        if(restaurant == null) return null;

        // Calculate distance
        int distance = Utils.distanceInMeters(
                currentLocation.latitude, currentLocation.longitude,
                restaurant.getLatitude(), restaurant.getLongitude()
        );

        // Update restaurant info
        return applyRestaurantDetails(restaurant, detailsResult, distance);
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

    public void setLastKnowLocation(LatLng lastKnowLocation) {
        mLastKnowLocation.setValue(lastKnowLocation);
    }

    public LiveData<Boolean> getLocationPermissionState() {
        return mLocationPermission;
    }

    public void setLocationPermissionState(boolean state){
        mLocationPermission.setValue(state);
    }

    public LiveData<List<Restaurant>> getRestaurantsLiveData() {
        return mRestaurantsLiveData;
    }
}
