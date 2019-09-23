package com.apiman.go4lunch.ui;

import android.Manifest;
import android.app.Activity;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.Objects;

public abstract class BaseFragment extends Fragment {

    protected BaseViewModel mViewModel;
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

    protected void grantLocationPermission() {
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
                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                }
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

}
