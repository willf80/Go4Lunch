package com.apiman.go4lunch;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apiman.go4lunch.adapters.WorkmateListAdapter;
import com.apiman.go4lunch.models.Restaurant;
import com.apiman.go4lunch.models.Workmate;
import com.apiman.go4lunch.fragments.ListViewFragment;
import com.apiman.go4lunch.viewmodels.RestaurantDetailsViewModel;
import com.apiman.go4lunch.viewmodels.WorkmatesViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class RestaurantDetailsActivity extends BaseActivity {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.markAsSelected)
    FloatingActionButton markAsSelectedFab;

    @BindView(R.id.nameTextView)
    TextView nameTextView;

    @BindView(R.id.addressTextView)
    TextView addressTextView;

    @BindView(R.id.callBtn)
    Button callBtn;

    @BindView(R.id.likeBtn)
    Button likeBtn;

    @BindView(R.id.websiteBtn)
    Button websiteBtn;

    RestaurantDetailsViewModel mDetailsViewModel;
    WorkmatesViewModel mWorkmatesViewModel;
    WorkmateListAdapter mWorkmateListAdapter;
    List<Workmate> mWorkmateList = new ArrayList<>();

    Realm mRealm;
    String mPlaceId;
    Restaurant mRestaurant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        displayHomeAsUp();

        mRealm = Realm.getDefaultInstance();


        mPlaceId = getIntent().getStringExtra(ListViewFragment.EXTRA_PLACE_ID);
        fetchRestaurant();

        mWorkmatesViewModel = ViewModelProviders
                .of(this)
                .get(WorkmatesViewModel.class);

        mDetailsViewModel = ViewModelProviders
                .of(this)
                .get(RestaurantDetailsViewModel.class);

        setupRecyclerView();
        getData();

        listeners();
        applyInfo();
    }

    private void listeners() {
        mDetailsViewModel
            .updatedListener()
            .observe(this, aVoid -> {
                Toast.makeText(RestaurantDetailsActivity.this, "Booking done !", Toast.LENGTH_SHORT).show();
            });


        mDetailsViewModel
            .errorDispatcherLister()
            .observe(this, s -> {
                Toast.makeText(RestaurantDetailsActivity.this, s, Toast.LENGTH_SHORT).show();
                Log.d("TAG_RESTAURANT", s);
            });

        markAsSelectedFab.setOnClickListener(view -> mDetailsViewModel.markRestaurantAsSelected(mPlaceId));
    }

    private void fetchRestaurant() {
        mRestaurant = mRealm.where(Restaurant.class)
                .equalTo("placeId", mPlaceId)
                .findFirst();
    }

    private void applyInfo() {
        if(mRestaurant == null) {
            finish();
            return;
        }

        nameTextView.setText(mRestaurant.getName());
        addressTextView.setText(mRestaurant.getAddress());

        if(mRestaurant.getWebsite() == null) {
            websiteBtn.setVisibility(View.GONE);
        }

        if(mRestaurant.getPhoneNumber() == null) {
            callBtn.setVisibility(View.GONE);
        }
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
