package com.apiman.go4lunch.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.apiman.go4lunch.R;
import com.apiman.go4lunch.models.Restaurant;
import com.apiman.go4lunch.fragments.BaseFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;

public class MapsFragment extends BaseFragment implements OnMapReadyCallback {
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final float DEFAULT_ZOOM = 15.0f;

    private GoogleMap mMap;
    private boolean mLocationPermissionGranted = false;
    private LatLng mDefaultLocation = new LatLng(40.6971494,-74.2598642);

    // Use fields to define the data types to return.
    private List<Place.Field> placeFields = Arrays.asList(
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ID,
            Place.Field.TYPES
        );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.fragment_maps, container, false);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if(mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Places.initialize(getContext(), getString(R.string.google_maps_key));

        return root;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Maps Settings
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setAllGesturesEnabled(true);
        uiSettings.setZoomControlsEnabled(true);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));

        mViewModel.getLocationPermissionState()
            .observe(this, state -> {
                mLocationPermissionGranted = state;
                updateLocationUI();
            });


        mViewModel.getLastKnowLocation()
            .observe(this, latLng -> {
                if(latLng == null) return;

                zoomToLocation(latLng);
                getRestaurants(getContext(), latLng);
            });
    }

    private void getRestaurants(Context context, LatLng latLng) {
        mViewModel.getRestaurantList(context, latLng)
                .observe(this, this::drawMarker);
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

            if(restaurant.isBook()) {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            }else{
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }

            mMap.addMarker(markerOptions);
        }
    }


//    private void findCurrentPlaceAndHandle() {
//        if(!canPlaceSearch) return;
//
//        // Use the builder to create a FindCurrentPlaceRequest.
//        FindCurrentPlaceRequest request =
//                FindCurrentPlaceRequest.newInstance(placeFields);
//
//        Task<FindCurrentPlaceResponse> placeResponse = mPlacesClient.findCurrentPlace(request);
//        placeResponse.addOnCompleteListener(task -> {
//            if (task.isSuccessful()){
//                FindCurrentPlaceResponse response = task.getResult();
//                mMap.clear();
//                for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
//                    List<Place.Type> placesType = placeLikelihood.getPlace().getTypes();
//                    if(placesType == null) continue;
//
//                    if(placesType.contains(Place.Type.RESTAURANT)){
//                        Log.i(TAG, String.format("Place '%s' has likelihood: %f",
//                                placeLikelihood.getPlace().getName(),
//                                placeLikelihood.getLikelihood()));
//                        Log.w(TAG, placeLikelihood.getPlace().getTypes().toString());
//
//                        MarkerOptions markerOptions = new MarkerOptions()
//                                .title(placeLikelihood.getPlace().getName())
//                                .position(placeLikelihood.getPlace().getLatLng());
//                        mMap.addMarker(markerOptions);
//                    }else {
//                        Log.e(TAG, String.format("Place '%s' has likelihood: %f",
//                                placeLikelihood.getPlace().getName(),
//                                placeLikelihood.getLikelihood()));
//                        Log.e(TAG, placeLikelihood.getPlace().getTypes().toString());
//                    }
////                    Log.i(TAG, String.format("Place '%s' has likelihood: %f",
////                            placeLikelihood.getPlace().getName(),
////                            placeLikelihood.getLikelihood()));
////                    Log.w(TAG, placeLikelihood.getPlace().getTypes().toString());
//                }
//            } else {
//                Exception exception = task.getException();
//                if (exception instanceof ApiException) {
//                    ApiException apiException = (ApiException) exception;
//                    Log.e(TAG, "Place not found: " + apiException.getStatusCode());
//                }
//            }
//        });
//    }

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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.app_bar_search){
            autoCompleteSearch();
        }
        return super.onOptionsItemSelected(item);
    }

    private void autoCompleteSearch() {
        LatLngBounds latLngBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        RectangularBounds bounds = RectangularBounds.newInstance(
                latLngBounds.southwest,
                latLngBounds.northeast);

        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, placeFields)
                .setCountry("fr")
                .setLocationBias(bounds)
                .build(getContext());
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }
}