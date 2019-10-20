package com.apiman.go4lunch.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apiman.go4lunch.R;
import com.apiman.go4lunch.adapters.RestaurantListAdapter;
import com.apiman.go4lunch.models.Restaurant;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ListViewFragment extends BaseFragment implements RestaurantListAdapter.OnDispatchListener {
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
        observeRestaurant();

        return root;
    }

    private void observeLastKnowLocation() {
        mViewModel
            .getLastKnowLocation()
            .observe(this, latLng -> {
                if(latLng == null) return;
                getRestaurants(getContext(), latLng);
            });
    }

    private void observeRestaurant() {
        mViewModel
            .getRestaurantsLiveData()
            .observe(this, restaurants -> {
                mRestaurantList = restaurants;
                mRestaurantListAdapter.setRestaurants(mRestaurantList);
            });
    }

    private void getRestaurants(Context context, LatLng latLng) {
        mViewModel.getRestaurantList(context, latLng);
    }

    @Override
    public void onItemClicked(Restaurant restaurant) {
        showRestaurantDetails(restaurant);
    }

    @Override
    void refreshData() {
        mViewModel.refreshData(getContext());
    }

    @Override
    protected void updateRating(String placeId, float rating) {
        Observable.fromIterable(mRestaurantList)
                .filter(restaurant -> Objects.equals(restaurant.getPlaceId(), placeId))
                .firstElement()
                .map(restaurant -> {
                    restaurant.setRating(rating);
                    return restaurant;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> mRestaurantListAdapter.setRestaurants(mRestaurantList))
                .subscribe();
    }
}