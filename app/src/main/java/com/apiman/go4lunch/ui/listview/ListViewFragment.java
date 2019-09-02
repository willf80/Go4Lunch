package com.apiman.go4lunch.ui.listview;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apiman.go4lunch.R;
import com.apiman.go4lunch.RestaurantDetailsActivity;
import com.apiman.go4lunch.adapters.RestaurantListAdapter;
import com.apiman.go4lunch.models.Restaurant;

import java.util.ArrayList;
import java.util.List;

public class ListViewFragment extends Fragment implements RestaurantListAdapter.OnDispatchListener {

    private ListViewModel mListViewModel;
    private RestaurantListAdapter mRestaurantListAdapter;
    private RecyclerView mRecyclerView;
    private List<Restaurant> mRestaurantList = new ArrayList<>();


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mListViewModel = ViewModelProviders.of(this).get(ListViewModel.class);

        View root = inflater.inflate(R.layout.fragment_list_view, container, false);
        mRecyclerView = root.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        mRestaurantListAdapter = new RestaurantListAdapter(mRestaurantList, this);
        mRecyclerView.setAdapter(mRestaurantListAdapter);

        mListViewModel.getRestaurantList()
                .observe(this, restaurants -> mRestaurantListAdapter.setRestaurants(restaurants));


        return root;
    }

    @Override
    public void onItemClicked(Restaurant restaurant) {
        Intent intent = new Intent(getContext(), RestaurantDetailsActivity.class);
        startActivity(intent);
    }
}