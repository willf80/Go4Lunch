package com.apiman.go4lunch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apiman.go4lunch.adapters.WorkmateJoiningAdapter;
import com.apiman.go4lunch.models.Restaurant;
import com.apiman.go4lunch.models.Workmate;
import com.apiman.go4lunch.services.FireStoreUtils;
import com.apiman.go4lunch.services.RestaurantStreams;
import com.apiman.go4lunch.viewmodels.RestaurantDetailsViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RestaurantDetailsActivity extends BaseActivity {

    public static final int BOOKED_RESULT_CODE = 8001;

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

    @BindView(R.id.markAsSelectedBtn)
    FloatingActionButton markAsSelectedBtn;

    @BindView(R.id.coverPhoto)
    ImageView coverPhoto;

    RestaurantDetailsViewModel mDetailsViewModel;
    WorkmateJoiningAdapter mWorkmateJoiningAdapter;
    List<Workmate> mWorkmateList = new ArrayList<>();

    String mPlaceId;
    String mPhotoReference;
    Restaurant mRestaurant;
    Disposable disposable;

    boolean isMyBooking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        displayHomeAsUp();

        mPlaceId = getIntent().getStringExtra(FireStoreUtils.FIELD_PLACE_ID);
        mPhotoReference = getIntent().getStringExtra(FireStoreUtils.FIELD_PHOTO);

        mDetailsViewModel = ViewModelProviders.of(this).get(RestaurantDetailsViewModel.class);

        setupRecyclerView();
        loadOnlineRestaurantDetails();
        listeners();

        mDetailsViewModel.getWorkmatesOfRestaurant(mPlaceId);
        mDetailsViewModel.checkIfRestaurantIsBookedByCurrentUser(mPlaceId);

        loadCoverPhoto();
    }

    private void setupRecyclerView(){
        mWorkmateJoiningAdapter = new WorkmateJoiningAdapter(mWorkmateList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mWorkmateJoiningAdapter);
    }

    private void listeners() {
        mDetailsViewModel
            .updatedListener()
            .observe(this, s -> {
                    mDetailsViewModel.getWorkmatesOfRestaurant(mPlaceId);
                    mDetailsViewModel.checkIfRestaurantIsBookedByCurrentUser(mPlaceId);
                    setResult(BOOKED_RESULT_CODE);

                    Toast.makeText(RestaurantDetailsActivity.this, "Restaurant booked successfully !", Toast.LENGTH_SHORT).show();
                }
            );

        mDetailsViewModel
            .errorDispatcherLister()
            .observe(this, s ->
                    Toast.makeText(RestaurantDetailsActivity.this, s, Toast.LENGTH_SHORT)
                        .show());

        mDetailsViewModel
                .getPlaceWorkmatesLiveData()
                .observe(this, bookings -> mWorkmateJoiningAdapter.setWorkmates(bookings));

        mDetailsViewModel.getMyBookedRestaurantLiveData()
                .observe(this, this::showBookedButton);
    }

    private void showBookedButton(boolean isMyBooking) {
        this.isMyBooking = isMyBooking;
        markAsSelectedBtn.show();

        if(isMyBooking) {
            markAsSelectedBtn.setImageResource(R.drawable.ic_cancel_black_24dp);
        }else {
            markAsSelectedBtn.setImageResource(R.drawable.ic_check_black_24dp);
        }
    }

    private void loadOnlineRestaurantDetails() {
        disposable = RestaurantStreams
                .getRestaurantDetailsExtractedFlowable(this, mPlaceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(restaurant -> {
                    mRestaurant = restaurant;
                    applyInfo();
                });
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
        }else {
            websiteBtn.setVisibility(View.VISIBLE);
        }

        if(mRestaurant.getPhoneNumber() == null) {
            callBtn.setVisibility(View.GONE);
        }else{
            callBtn.setVisibility(View.VISIBLE);
        }
    }

    private void loadCoverPhoto() {
        Picasso.get()
                .load(RestaurantStreams.getMediumPhotoUrl(this, mPhotoReference))
                .centerCrop()
                .resize(400, 400)
                .into(coverPhoto);
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

    void confirmBooking() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Do you want to book in this restaurant?") //Voulez-vous réserver dans ce restaurant?
                .setNegativeButton("No", null)
                .setPositiveButton("Yes, I want",
                        (dialog, which) -> mDetailsViewModel.markRestaurantAsSelected(mRestaurant))
                .create()
                .show();
    }

    void confirmCancelBooking() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Do you want to remove your booking ?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes, remove it",
                        (dialog, which) -> {
                            mDetailsViewModel.removeBooking(mRestaurant.getPlaceId());
                            setResult(BOOKED_RESULT_CODE);
                        })
                .create()
                .show();
    }

    @OnClick(R.id.markAsSelectedBtn)
    void doBooking() {
        if(isMyBooking) {
            confirmCancelBooking();
            return;
        }

        confirmBooking();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(disposable != null && !disposable.isDisposed()){
            disposable.dispose();
        }
    }
}
