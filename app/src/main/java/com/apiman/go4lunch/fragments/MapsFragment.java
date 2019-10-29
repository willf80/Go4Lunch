package com.apiman.go4lunch.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.apiman.go4lunch.R;
import com.apiman.go4lunch.models.Restaurant;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapsFragment extends BaseFragment implements OnMapReadyCallback {
    private static final float DEFAULT_ZOOM = 15.0f;

    private GoogleMap mMap;
    private boolean mLocationPermissionGranted = false;
    private LatLng mDefaultLocation = new LatLng(48.8555874,2.3890811);

    private Restaurant mRestaurantSelected;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.fragment_maps, container, false);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if(mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return root;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Maps Settings
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setAllGesturesEnabled(true);
        uiSettings.setZoomControlsEnabled(true);

        mMap.setOnMapLoadedCallback(this::updateLatLngBounds);
        mMap.setOnCameraIdleListener(this::updateLatLngBounds);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));

        mViewModel.getLocationPermissionState().observe(this, state -> {
            mLocationPermissionGranted = state;
            updateLocationUI();
        });

        mViewModel.getLastKnowLocation().observe(this, latLng -> {
            if(latLng == null) return;
            zoomToLocation(latLng);
            getRestaurants(getContext(), latLng);
        });

        onMarkerClicked();
        onInfoWindowClicked();
    }

    private void updateLatLngBounds() {
        mViewModel.setMapLatLngBounds(mMap.getProjection().getVisibleRegion().latLngBounds);
    }

    private void onMarkerClicked() {
        mMap.setOnMarkerClickListener(marker -> {
            mRestaurantSelected = (Restaurant) marker.getTag();
            return false;
        });
    }

    private void onInfoWindowClicked() {
        mMap.setOnInfoWindowClickListener(marker -> {
            if(mRestaurantSelected == null) return;
            startDetailsActivity(mRestaurantSelected);
        });
    }

    private void getRestaurants(Context context, LatLng latLng) {
        mViewModel.getRestaurantList(context, latLng).observe(this, this::drawMarker);
    }

    private void zoomToLocation(LatLng latLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
    }

    private void drawMarker(List<Restaurant> restaurants) {
        // Clean map
        mMap.clear();

        // Draw
        for (Restaurant restaurant : restaurants) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .title(restaurant.getName())
                    .position(new LatLng(restaurant.getLatitude(), restaurant.getLongitude()));

            if (restaurant.isBooked()) {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            } else {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }

            Marker marker = mMap.addMarker(markerOptions);
            marker.setTag(restaurant);
        }
    }

    private void updateLocationUI() {
        if (mMap == null) return;

        if (mLocationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            grantLocationPermission();
        }
    }
}