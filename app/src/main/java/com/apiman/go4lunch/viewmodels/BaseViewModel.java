package com.apiman.go4lunch.viewmodels;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.apiman.go4lunch.helpers.FireStoreUtils;
import com.apiman.go4lunch.helpers.Utils;
import com.apiman.go4lunch.models.ApiResponse;
import com.apiman.go4lunch.models.Restaurant;
import com.apiman.go4lunch.models.SearchMode;
import com.apiman.go4lunch.services.RestaurantStreams;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.Tasks;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.apiman.go4lunch.helpers.FireStoreUtils.FIELD_PLACE_ID;

public class BaseViewModel extends ViewModel {
    private static final String TAG = "BaseViewModel";

    // Location permission state
    private MutableLiveData<Boolean> mLocationPermission;

    // Last location
    private MutableLiveData<LatLng> mLastKnowLocation;

    // List of restaurants
    private MutableLiveData<List<Restaurant>> mRestaurantsLiveData;

    private LatLng lastLatLng;
    private List<Restaurant> mRestaurantList;

    private LatLngBounds mMapLatLngBounds;
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

    private int calculateDistance(LatLng currentPosition, LatLng restaurantPosition) {
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
                    int distance = calculateDistance(latLng,
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
                .map(detailsResponse -> RestaurantStreams.applyRestaurantDetails(context, restaurant, detailsResponse.getApiResult()))
                .map(restaurantWithDetails -> {
                    QuerySnapshot snapshot = Tasks.await(FireStoreUtils.getRestaurantRatingScore(restaurant.getPlaceId()).get());
                    Float rating = FireStoreUtils.averageRating(snapshot);
                    restaurantWithDetails.setRating(rating);

                    return restaurantWithDetails;
                })
                .doOnError(throwable -> {});
    }

    private Flowable<List<Restaurant>> getRestaurantsDetailsFlowable(final Context context, List<Restaurant> restaurants) {
        // Create iterable Flowable
        return Flowable.fromIterable(restaurants)
                .parallel()
                .runOn(Schedulers.io())
                .flatMap(restaurant -> updateRestaurantItemFlowable(context, restaurant))
                .map(restaurant -> {
                    LatLng restaurantLatLng = new LatLng(restaurant.getLatitude(), restaurant.getLongitude());
                    int distance = calculateDistance(lastLatLng, restaurantLatLng);
                    restaurant.setDistance(distance);
                    return restaurant;
                })
                .toSortedList((o1, o2) -> o1.getDistance() - o2.getDistance())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private void getRestaurantsDetails(final Context context) {
        // Create iterable Flowable
        getRestaurantsDetailsFlowable(context, mRestaurantList)
            .doFinally(() -> mRestaurantsLiveData.setValue(mRestaurantList))
            .subscribe();
    }

    private void getRestaurantsDetailsForSearch(final Context context, List<Restaurant> restaurants) {
        // Create iterable Flowable
        getRestaurantsDetailsFlowable(context, restaurants)
                .doFinally(() -> mRestaurantsLiveData.setValue(restaurants))
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
        getTodayBookingFlowable(mRestaurantList)
                .doFinally(() -> {
                    mRestaurantsLiveData.setValue(mRestaurantList);
                    getRestaurantsDetails(context);
                })
                .subscribe();
    }

    private Flowable<Restaurant> getTodayBookingFlowable(List<Restaurant> restaurants) {
        return Flowable.just(restaurants)
                .flatMap(restaurantList -> {
                    QuerySnapshot query = FireStoreUtils.getTodayBookingAwait();
                    return updateRestaurantsBookedInfoFlowable(restaurantList, query.getDocuments());
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private FindAutocompletePredictionsRequest createRequest(String query) {
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        RectangularBounds bounds = RectangularBounds.newInstance(
                mMapLatLngBounds.southwest,
                mMapLatLngBounds.northeast);

        return FindAutocompletePredictionsRequest
                .builder()
                .setLocationBias(bounds)
                .setCountry("fr")
                .setSessionToken(token)
                .setQuery(query)
                .build();
    }

    public void searchAutoComplete(Context context, PlacesClient placesClient, String query) {
        if(searchMode == SearchMode.WORKMATES) {
            return;
        }

        placesClient.findAutocompletePredictions(createRequest(query))
                .addOnSuccessListener(response -> {
                    List<String> placeIdList = new ArrayList<>();
                    for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                        //Log.e("BASE", prediction.getFullText(null) + " : " + prediction.getPlaceId());
                        List<Place.Type> placesType = prediction.getPlaceTypes();
                        if(!placesType.contains(Place.Type.RESTAURANT)) continue;

                        Log.e(TAG, prediction.getFullText(null) + " : " + prediction.getPlaceId());
//                        if(placeIdList.size() <= 0)
                        placeIdList.add(prediction.getPlaceId());
                    }

                    getDetailsOfPlaceId(context, placeIdList);
                });
    }

    private void getDetailsOfPlaceId(Context context, List<String> placeIds) {
        mDisposable = Flowable.fromIterable(placeIds)
                .parallel()
                .runOn(Schedulers.io())
                .flatMap(placeId -> RestaurantStreams.getRestaurantDetailsExtractedFlowable(context, placeId))
                .map(restaurant -> {
                    LatLng restaurantLatLng = new LatLng(restaurant.getLatitude(), restaurant.getLongitude());
                    int distance = calculateDistance(lastLatLng, restaurantLatLng);
                    restaurant.setDistance(distance);
                    return restaurant;
                })
                .toSortedList((o1, o2) -> o1.getDistance() - o2.getDistance())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(throwable -> Log.e(TAG, "Auto complete details error ", throwable))
                .subscribe(restaurants -> {
                    mRestaurantsLiveData.setValue(restaurants);

                    getTodayBookingFlowable(restaurants)
                            .doFinally(() -> getRestaurantsDetailsForSearch(context, restaurants))
                            .subscribe();
                });
    }

//    private void getDetailsOfPlaceIdWithoutPhoto(PlacesClient placesClient, List<String> placeIds) {
//        mDisposable = Flowable.fromIterable(placeIds)
//                .parallel()
//                .runOn(Schedulers.io())
//                .flatMap(s -> {
//                    FetchPlaceResponse placeResponse = Tasks.await(placesClient.fetchPlace(getFetchPlaceRequest(s)));
//
//                    if(placeResponse == null) return Flowable.empty();
//                    Place place = placeResponse.getPlace();
//
//                    Restaurant restaurant = new Restaurant();
//                    restaurant.setPlaceId(place.getId());
//                    restaurant.setName(place.getName());
//                    restaurant.setAddress(place.getAddress());
//
//                    List<PhotoMetadata> photoMetadataList = place.getPhotoMetadatas();
//                    if(photoMetadataList != null && photoMetadataList.size() > 0) {
//                        PhotoMetadata photoMetadata = photoMetadataList.get(0);
//                        Log.e(TAG, photoMetadata.getAttributions());
//                        //FetchPhotoRequest.builder(photoMetadata).build(
//
//                    }
//
//                    LatLng latLng = place.getLatLng();
//                    if(latLng != null) {
//                        restaurant.setLatitude(latLng.latitude);
//                        restaurant.setLongitude(latLng.longitude);
//
//                        int distance = calculateDistance(lastLatLng, latLng);
//                        restaurant.setDistance(distance);
//                    }
//
//                    return Flowable.just(restaurant);
//                })
//                .sequential()
//                .toSortedList((o1, o2) -> o1.getDistance() - o2.getDistance())
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .doOnError(throwable -> Log.e(TAG, "Auto complete details error ", throwable))
//                .subscribe(restaurants -> mRestaurantsLiveData.setValue(restaurants));
//    }
//    private FetchPlaceRequest getFetchPlaceRequest(String placeId) {
//        List<Place.Field> placeFields = Arrays.asList(
//                Place.Field.ID,
//                Place.Field.NAME,
//                Place.Field.LAT_LNG,
//                Place.Field.ADDRESS,
//                Place.Field.PHOTO_METADATAS);
//        return  FetchPlaceRequest.newInstance(placeId, placeFields);
//    }

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

    public void setMapLatLngBounds(LatLngBounds mapLatLngBounds) {
        this.mMapLatLngBounds = mapLatLngBounds;
    }
}
