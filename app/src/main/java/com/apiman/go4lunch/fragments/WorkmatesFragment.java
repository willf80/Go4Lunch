package com.apiman.go4lunch.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apiman.go4lunch.R;
import com.apiman.go4lunch.adapters.WorkmateBookingAdapter;
import com.apiman.go4lunch.models.WorkmateBooking;
import com.apiman.go4lunch.viewmodels.WorkmatesViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WorkmatesFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private WorkmateBookingAdapter mWorkmateBookingAdapter;
    private List<WorkmateBooking> mWorkmateBookings = new ArrayList<>();

    private WorkmatesViewModel mWorkmatesViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mWorkmatesViewModel = ViewModelProviders
                .of(Objects.requireNonNull(getActivity()))
                .get(WorkmatesViewModel.class);

        View root = inflater.inflate(R.layout.fragment_workmates, container, false);
        mRecyclerView = root.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mWorkmateBookingAdapter = new WorkmateBookingAdapter(mWorkmateBookings);
        mRecyclerView.setAdapter(mWorkmateBookingAdapter);

        workmatesListener();

        //Load workmates
        mWorkmatesViewModel.loadWorkmates();

        return root;
    }

    private void workmatesListener() {
        mWorkmatesViewModel.getWorkmatesLiveData().observe(this,
                workmateList -> mWorkmateBookingAdapter.setWorkmateBookings(workmateList));
    }
}