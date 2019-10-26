package com.apiman.go4lunch.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.apiman.go4lunch.R;
import com.apiman.go4lunch.RestaurantDetailsActivity;
import com.apiman.go4lunch.models.Restaurant;
import com.apiman.go4lunch.viewmodels.BaseViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.apiman.go4lunch.helpers.FireStoreUtils.FIELD_PHOTO;
import static com.apiman.go4lunch.helpers.FireStoreUtils.FIELD_PLACE_ID;

public abstract class BaseFragment extends Fragment {
    private static final int BOOKING_REQUEST_CODE = 8000;

    BaseViewModel mViewModel;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Activity mActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();

        mViewModel = ViewModelProviders
                .of(Objects.requireNonNull(getActivity()))
                .get(BaseViewModel.class);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        observers();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mActivity);
        getLastLocation();
    }

    private void observers() {
        mViewModel.getLocationPermissionState().observe(this, state -> {
            if(state) return;
            grantLocationPermission();
        });
    }

    void grantLocationPermission() {
        Dexter.withActivity(getActivity())
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(new PermissionListener() {
                @Override
                public void onPermissionGranted(PermissionGrantedResponse response) {
                    mViewModel.setLocationPermissionState(true);
                }

                @Override
                public void onPermissionDenied(PermissionDeniedResponse response) {
                    mViewModel.setLocationPermissionState(false);
                }

                @Override
                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) { }
            })
            .check();
    }

    private void getLastLocation() {
        mFusedLocationProviderClient
        .getLastLocation()
        .addOnCompleteListener(mActivity, task -> {
            if (task.isSuccessful()) {
                Location location = task.getResult();
                if(location == null) return;
                mViewModel.setLastKnowLocation(
                        new LatLng(location.getLatitude(), location.getLongitude()));
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == BOOKING_REQUEST_CODE &&
                resultCode == RestaurantDetailsActivity.BOOKED_RESULT_CODE) {

            if(data != null) {
                float rating = data.getFloatExtra(RestaurantDetailsActivity.EXTRA_DATA_RATING_KEY, -1);
                String placeId = data.getStringExtra(RestaurantDetailsActivity.EXTRA_DATA_PLACE_KEY);
                if(rating >= 0 && placeId != null){
                    updateRating(placeId, rating);
                    return;
                }
            }

            refreshData();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
    }

    void startDetailsActivity(Restaurant restaurant) {
        Intent intent = new Intent(getContext(), RestaurantDetailsActivity.class);
        intent.putExtra(FIELD_PLACE_ID, restaurant.getPlaceId());
        intent.putExtra(FIELD_PHOTO, restaurant.getPhotoReference());
        startActivityForResult(intent, BOOKING_REQUEST_CODE);
    }

    /**
     * Called after user rating a restaurant
     * @param placeId Place ID
     * @param rating rating average
     */
    private void updateRating(String placeId, float rating) {
        List<Restaurant> restaurantList = mViewModel.getRestaurantsLiveData().getValue();
        if(restaurantList == null) return;

        Observable.fromIterable(restaurantList)
                .filter(restaurant -> Objects.equals(restaurant.getPlaceId(), placeId))
                .firstElement()
                .map(restaurant -> {
                    restaurant.setRating(rating);
                    return restaurant;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> mViewModel.setRestaurants(restaurantList))
                .subscribe();
    }

    /**
     * Called to refresh all data
     */
    private void refreshData() {
        mViewModel.refreshData(getContext());
    }

}
