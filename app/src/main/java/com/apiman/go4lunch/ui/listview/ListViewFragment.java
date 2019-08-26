package com.apiman.go4lunch.ui.listview;

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
import com.apiman.go4lunch.adapters.RestaurantListAdapter;
import com.apiman.go4lunch.models.Restaurant;

import java.util.ArrayList;
import java.util.List;

public class ListViewFragment extends Fragment {

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


        mRestaurantListAdapter = new RestaurantListAdapter(mRestaurantList);
        mRecyclerView.setAdapter(mRestaurantListAdapter);
//        final TextView textView = root.findViewById(R.id.text_home);
//        mListViewModel.getText().observe(this, new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });

        mListViewModel.getRestaurantList()
                .observe(this, restaurants -> mRestaurantListAdapter.setRestaurants(restaurants));


        return root;
    }
}