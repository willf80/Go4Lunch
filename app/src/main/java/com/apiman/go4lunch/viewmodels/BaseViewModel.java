package com.apiman.go4lunch.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.apiman.go4lunch.helpers.FireStoreUtils;
import com.apiman.go4lunch.helpers.Utils;
import com.apiman.go4lunch.models.ApiDetailsResult;
import com.apiman.go4lunch.models.ApiResponse;
import com.apiman.go4lunch.models.Period;
import com.apiman.go4lunch.models.Restaurant;
import com.apiman.go4lunch.models.SearchMode;
import com.apiman.go4lunch.services.RestaurantStreams;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Tasks;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.apiman.go4lunch.helpers.FireStoreUtils.FIELD_PLACE_ID;

public class BaseViewModel extends ViewModel {
    // Location permission state
    private MutableLiveData<Boolean> mLocationPermission;

    // Last location
    private MutableLiveData<LatLng> mLastKnowLocation;

    // List of restaurants
    private MutableLiveData<List<Restaurant>> mRestaurantsLiveData;

    private LatLng lastLatLng;
    private List<Restaurant> mRestaurantList;
    private List<Restaurant> mSearchRestaurantList;

    private SearchMode searchMode = SearchMode.RESTAURANTS;
    private Disposable mDisposable;

    public BaseViewModel() {
        mLocationPermission = new MutableLiveData<>();
        mLocationPermission.setValue(false);

        mLastKnowLocation = new MutableLiveData<>();
        mRestaurantsLiveData = new MutableLiveData<>();
        mRestaurantList = new ArrayList<>();
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
                });
    }

    private Flowable<Restaurant> updateRestaurantItemFlowable(Context context, Restaurant restaurant) {
        return RestaurantStreams
                .getRestaurantDetailsFlowable(context, restaurant.getPlaceId())
                .map(detailsResponse -> applyRestaurantDetails(restaurant, detailsResponse.getApiResult()))
                .map(restaurantWithDetails -> {
                    QuerySnapshot snapshot = Tasks.await(FireStoreUtils.getRestaurantRatingScore(restaurant.getPlaceId()).get());
                    Float rating = FireStoreUtils.averageRating(snapshot);
                    restaurantWithDetails.setRating(rating);

                    return restaurantWithDetails;
                });
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

    private FindAutocompletePredictionsRequest createRequest(String query) {
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        return FindAutocompletePredictionsRequest
                .builder()
                .setCountry("fr")
                .setSessionToken(token)
                .setQuery(query)
                .build();
    }

    public void searchAutoComplete(PlacesClient placesClient, String query) {
//        Log.e("BASE SEARCH -> ", query);
        if(searchMode == SearchMode.WORKMATES) {
            return;
        }

        placesClient.findAutocompletePredictions(createRequest(query))
                .addOnSuccessListener(response -> {
                    mSearchRestaurantList = new ArrayList<>();
                    for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
//                        Log.e("BASE", prediction.getFullText(null) + " : " + prediction.getPlaceId());
                        List<Place.Type> placesType = prediction.getPlaceTypes();
                        if(!placesType.contains(Place.Type.RESTAURANT)) continue;

                        Restaurant restaurantFound = Observable.fromIterable(mRestaurantList)
                                .filter(restaurant -> Objects.equals(restaurant.getPlaceId(), prediction.getPlaceId()))
                                .blockingFirst(null);

                        //Place place; place.
                        if(restaurantFound != null) mSearchRestaurantList.add(restaurantFound);
                    }

                    mRestaurantsLiveData.setValue(mSearchRestaurantList);
                });
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

    public void closeSearchMode() {
        mRestaurantsLiveData.setValue(mRestaurantList);
    }

    public void setSearchMode(SearchMode searchMode) {
        this.searchMode = searchMode;
    }
}
