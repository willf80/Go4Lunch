package com.apiman.go4lunch.fragments;

import android.content.Intent;
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
import com.apiman.go4lunch.RestaurantDetailsActivity;
import com.apiman.go4lunch.adapters.WorkmateBookingAdapter;
import com.apiman.go4lunch.models.WorkmateBooking;
import com.apiman.go4lunch.services.FireStoreUtils;
import com.apiman.go4lunch.viewmodels.WorkmatesViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WorkmatesFragment extends Fragment implements WorkmateBookingAdapter.DispatchListener {

    private WorkmateBookingAdapter mWorkmateBookingAdapter;
    private List<WorkmateBooking> mWorkmateBookings = new ArrayList<>();
    private WorkmatesViewModel mWorkmatesViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mWorkmatesViewModel = ViewModelProviders
                .of(Objects.requireNonNull(getActivity()))
                .get(WorkmatesViewModel.class);

        View root = inflater.inflate(R.layout.fragment_workmates, container, false);
        RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mWorkmateBookingAdapter = new WorkmateBookingAdapter(mWorkmateBookings, this);
        recyclerView.setAdapter(mWorkmateBookingAdapter);

        workmatesListener();

        //Load workmates
        mWorkmatesViewModel.loadWorkmates();

        return root;
    }

    private void workmatesListener() {
        mWorkmatesViewModel.getWorkmatesLiveData().observe(this,
                workmateList -> mWorkmateBookingAdapter.setWorkmateBookings(workmateList));
    }

    @Override
    public void onClick(WorkmateBooking workmateBooking) {
        if(workmateBooking.booking == null) return;

        Intent intent = new Intent(getContext(), RestaurantDetailsActivity.class);
        intent.putExtra(FireStoreUtils.FIELD_PLACE_ID, workmateBooking.booking.placeId);
        intent.putExtra(FireStoreUtils.FIELD_PHOTO, workmateBooking.booking.restaurantPhoto);
        startActivity(intent);
    }
}