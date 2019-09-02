package com.apiman.go4lunch;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apiman.go4lunch.adapters.WorkmateListAdapter;
import com.apiman.go4lunch.models.Workmate;
import com.apiman.go4lunch.ui.workmates.WorkmatesViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RestaurantDetailsActivity extends BaseActivity {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    WorkmatesViewModel mWorkmatesViewModel;
    WorkmateListAdapter mWorkmateListAdapter;
    List<Workmate> mWorkmateList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        displayHomeAsUp();

        mWorkmatesViewModel = ViewModelProviders.of(this)
                .get(WorkmatesViewModel.class);


        fab.setOnClickListener(view ->
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        setupRecyclerView();
        getData();
    }

    private void setupRecyclerView(){
        mWorkmateListAdapter = new WorkmateListAdapter(mWorkmateList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mWorkmateListAdapter);
    }

    private void getData(){
        mWorkmatesViewModel.getWorkmateJoinsList()
                .observe(this, workmates -> mWorkmateListAdapter.setWorkmates(workmates));
    }
}
