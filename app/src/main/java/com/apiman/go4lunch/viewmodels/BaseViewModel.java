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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.apiman.go4lunch.services.FireStoreUtils.FIELD_PLACE_ID;

public class BaseViewModel extends ViewModel {

    // Location permission state
    private MutableLiveData<Boolean> mLocationPermission;

    // Last location
    private MutableLiveData<LatLng> mLastKnowLocation;

    // List of restaurants
    private MutableLiveData<List<Restaurant>> mRestaurantsLiveData;

//    private Realm mRealm;
//    private CollectionReference todayBooksRef;
    private LatLng lastLatLng;
    private List<Restaurant> mRestaurantList;
    private Disposable mDisposable;

    public BaseViewModel() {
//        mRealm = Realm.getDefaultInstance();

        mLocationPermission = new MutableLiveData<>();
        mLocationPermission.setValue(false);

        mLastKnowLocation = new MutableLiveData<>();
        mRestaurantsLiveData = new MutableLiveData<>();
        mRestaurantList = new ArrayList<>();

//        todayBooksRef = FireStoreUtils.getTodayBookingCollection();
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
        mRestaurantsLiveData.setValue(new ArrayList<>());
        loadRestaurants(context, lastLatLng);
    }


    private int performDistance(LatLng currentPosition, LatLng restaurantPosition) {
        return Utils.distanceInMeters(
                currentPosition.latitude, currentPosition.longitude,
                restaurantPosition.latitude, restaurantPosition.longitude);
    }

    private void loadRestaurants(final Context context, LatLng latLng) {
        if(latLng == null) return;

        mDisposable = RestaurantStreams.getNearbyRestaurantsObservable(context, latLng)
                .map(ApiResponse::getRestaurants)
                .flatMapIterable(restaurants -> restaurants)
                .parallel()
                .runOn(Schedulers.io())
                .map(restaurant -> {
                    int distance = performDistance(latLng,
                            new LatLng(restaurant.getLatitude(), restaurant.getLongitude()));
                    restaurant.setDistance(distance);
                    return restaurant;
                })
                .sorted((o1, o2) -> (o1.getDistance() - o2.getDistance()))
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(restaurants -> {
                    mRestaurantList = restaurants;
                    getTodayBooking(context);// Get Cloud FireStore booking places
//                    mRestaurantsLiveData.setValue(restaurants);
                });
    }

    private Flowable<Restaurant> updateRestaurantItemFlowable(Context context, Restaurant restaurant) {
        return RestaurantStreams
                .getRestaurantDetailsFlowable(context, restaurant.getPlaceId())
                .map(detailsResponse -> applyRestaurantDetails(restaurant, detailsResponse.getApiResult()));
    }

    private void getRestaurantsDetails(final Context context, List<Restaurant> restaurants) {
        // Create iterable Flowable
        Flowable.fromIterable(restaurants)
            .parallel()
            .runOn(Schedulers.io())
            .flatMap(restaurant -> updateRestaurantItemFlowable(context, restaurant))
            .sequential()
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally(() -> mRestaurantsLiveData.setValue(mRestaurantList))
            .subscribe();
    }

    private Restaurant updateRestaurantBookedItem(List<Restaurant> restaurants, List<DocumentSnapshot> documentBookedList) {
        if(documentBookedList.size() <= 0) {
            return null;
        }

        DocumentSnapshot doc = documentBookedList.get(0);
        String key = doc.get(FIELD_PLACE_ID, String.class);

        Restaurant restaurantItem = Flowable.fromIterable(restaurants)
                .filter(restaurantFilter -> Objects.equals(restaurantFilter.getPlaceId(), key))
                .blockingFirst();

        if(restaurantItem == null) {
            return null;
        }

        restaurantItem.setTotalWorkmates(documentBookedList.size());
        restaurantItem.setBooked(!documentBookedList.isEmpty());

        return restaurantItem;
    }

    private Flowable<Restaurant> updateRestaurantsBookedInfoFlowable(List<Restaurant> restaurants, List<DocumentSnapshot> allBooking) {
        return Flowable.fromIterable(allBooking)
                .groupBy(documentSnapshot -> documentSnapshot.get(FIELD_PLACE_ID, String.class))
                .flatMapSingle(Flowable::toList)
                .parallel()
                .runOn(Schedulers.io())
                .map(documentSnapshots -> updateRestaurantBookedItem(restaurants, documentSnapshots))
                .sequential();
    }

    // Get Cloud FireStore booking places
    private void getTodayBooking(Context context) {
        Flowable.just(mRestaurantList)
                .flatMap(restaurants -> {
                    QuerySnapshot query = FireStoreUtils.getTodayBookingAwait();
                    return updateRestaurantsBookedInfoFlowable(restaurants, query.getDocuments());
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> {
                    mRestaurantsLiveData.setValue(mRestaurantList);
                    getRestaurantsDetails(context, mRestaurantList);
                })
                .subscribe();
    }

    private Restaurant applyRestaurantDetails(Restaurant restaurant, ApiDetailsResult detailsResult){
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

        return restaurant;
    }

    //---- END

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

    @Override
    protected void onCleared() {
        super.onCleared();
        if(mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }
}
