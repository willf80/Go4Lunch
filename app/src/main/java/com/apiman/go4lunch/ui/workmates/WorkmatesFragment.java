package com.apiman.go4lunch.ui.workmates;

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
import com.apiman.go4lunch.adapters.WorkmateListAdapter;
import com.apiman.go4lunch.models.Workmate;

import java.util.ArrayList;
import java.util.List;

public class WorkmatesFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private WorkmateListAdapter mWorkmateListAdapter;
    private List<Workmate> mWorkmateList = new ArrayList<>();

    private WorkmatesViewModel mWorkmatesViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mWorkmatesViewModel = ViewModelProviders.of(this).get(WorkmatesViewModel.class);
        View root = inflater.inflate(R.layout.fragment_workmates, container, false);
        mRecyclerView = root.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mWorkmateListAdapter = new WorkmateListAdapter(mWorkmateList);
        mRecyclerView.setAdapter(mWorkmateListAdapter);

        mWorkmatesViewModel.getWorkmateList().observe(this,
                workmateList -> mWorkmateListAdapter.setWorkmates(workmateList));
        return root;
    }
}