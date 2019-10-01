package com.apiman.go4lunch.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apiman.go4lunch.R;
import com.apiman.go4lunch.RestaurantDetailsActivity;
import com.apiman.go4lunch.adapters.RestaurantListAdapter;
import com.apiman.go4lunch.models.Restaurant;
import com.apiman.go4lunch.fragments.BaseFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class ListViewFragment extends BaseFragment implements RestaurantListAdapter.OnDispatchListener {
    public static final String EXTRA_PLACE_ID = "placeId";

    private RestaurantListAdapter mRestaurantListAdapter;
    private List<Restaurant> mRestaurantList = new ArrayList<>();


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.fragment_list_view, container, false);
        RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mRestaurantListAdapter = new RestaurantListAdapter(mRestaurantList, this);
        recyclerView.setAdapter(mRestaurantListAdapter);

        observeLastKnowLocation();

        return root;
    }

    private void observeLastKnowLocation() {
        mViewModel.getLastKnowLocation()
                .observe(this, latLng -> {
                    if(latLng == null) return;

                    getRestaurants(getContext(), latLng);
                });
    }

    private void getRestaurants(Context context, LatLng latLng) {
        mViewModel.getRestaurantList(context, latLng)
                .observe(this, restaurants ->
                        mRestaurantListAdapter.setRestaurants(restaurants));
    }

    @Override
    public void onItemClicked(Restaurant restaurant) {
        Intent intent = new Intent(getContext(), RestaurantDetailsActivity.class);
        intent.putExtra(EXTRA_PLACE_ID, restaurant.getPlaceId());
        startActivity(intent);
    }
}