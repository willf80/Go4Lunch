package com.apiman.go4lunch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apiman.go4lunch.adapters.WorkmateJoiningAdapter;
import com.apiman.go4lunch.fragments.ListViewFragment;
import com.apiman.go4lunch.models.Restaurant;
import com.apiman.go4lunch.models.Workmate;
import com.apiman.go4lunch.viewmodels.RestaurantDetailsViewModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

public class RestaurantDetailsActivity extends BaseActivity {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

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
    WorkmateJoiningAdapter mWorkmateJoiningAdapter;
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

        mDetailsViewModel = ViewModelProviders
                .of(this)
                .get(RestaurantDetailsViewModel.class);

        setupRecyclerView();

        listeners();
        applyInfo();

        mDetailsViewModel.getWorkmatesOfRestaurant(mPlaceId);
    }

    private void listeners() {
        mDetailsViewModel
            .updatedListener()
            .observe(this, s -> {
                    mDetailsViewModel.getWorkmatesOfRestaurant(mPlaceId);
                    Toast.makeText(RestaurantDetailsActivity.this, "Booking done !", Toast.LENGTH_SHORT).show();
                }
            );

        mDetailsViewModel
            .errorDispatcherLister()
            .observe(this, s -> {
                Toast.makeText(RestaurantDetailsActivity.this, s, Toast.LENGTH_SHORT).show();
                Log.d("TAG_RESTAURANT", s);
            });

        mDetailsViewModel.getPlaceWorkmatesLiveData()
                .observe(this, workmates -> mWorkmateJoiningAdapter.setWorkmates(workmates));
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
        mWorkmateJoiningAdapter = new WorkmateJoiningAdapter(mWorkmateList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mWorkmateJoiningAdapter);
    }

    @OnClick(R.id.callBtn)
    void call() {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", mRestaurant.getPhoneNumber(), null));
        startActivity(intent);
    }

    @OnClick(R.id.websiteBtn)
    void showWebSite() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mRestaurant.getWebsite()));
        startActivity(intent);
    }

    @OnClick(R.id.markAsSelectedBtn)
    void confirmSelection() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Do you want to book in this restaurant?") //Voulez-vous réserver dans ce restaurant?
                .setNegativeButton("No", null)
                .setPositiveButton("Yes, I want",
                        (dialog, which) -> mDetailsViewModel.markRestaurantAsSelected(mRestaurant))
                .create()
                .show();
    }
}
