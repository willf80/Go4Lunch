package com.apiman.go4lunch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apiman.go4lunch.adapters.WorkmateJoiningAdapter;
import com.apiman.go4lunch.fragments.ProgressDialogFragment;
import com.apiman.go4lunch.fragments.RatingDialogFragment;
import com.apiman.go4lunch.helpers.FireStoreUtils;
import com.apiman.go4lunch.models.Rating;
import com.apiman.go4lunch.models.Restaurant;
import com.apiman.go4lunch.models.Workmate;
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

public class RestaurantDetailsActivity extends BaseActivity implements RatingDialogFragment.OnRatingFragmentInteractionListener {

    public static final int BOOKED_RESULT_CODE = 8001;
    public static final String EXTRA_DATA_RATING_KEY = "ratingAvg";
    public static final String EXTRA_DATA_PLACE_KEY = "placeId";

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

    @BindView(R.id.ratingBar)
    RatingBar ratingBar;

    RestaurantDetailsViewModel mDetailsViewModel;
    WorkmateJoiningAdapter mWorkmateJoiningAdapter;
    List<Workmate> mWorkmateList = new ArrayList<>();

    String mPlaceId;
    String mPhotoReference;
    Restaurant mRestaurant;
    Disposable disposable;

    boolean isMyBooking = false;
    boolean isRated = false;

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
        mDetailsViewModel.getRestaurantRating(mPlaceId);

        loadCoverPhoto();
    }

    private void setupRecyclerView(){
        mWorkmateJoiningAdapter = new WorkmateJoiningAdapter(mWorkmateList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mWorkmateJoiningAdapter);
    }

    private void restaurantSuccessObserver() {
        mDetailsViewModel
            .updatedListener()
            .observe(this, s -> {
                    mDetailsViewModel.getWorkmatesOfRestaurant(mPlaceId);
                    mDetailsViewModel.checkIfRestaurantIsBookedByCurrentUser(mPlaceId);
                    setResult(BOOKED_RESULT_CODE);

                    Toast.makeText(
                            RestaurantDetailsActivity.this,
                            getString(R.string.restaurant_booked_success),
                            Toast.LENGTH_SHORT).show();
                }
            );
    }

    private void restaurantDetailsErrorObserver() {
        mDetailsViewModel
                .errorDispatcherLister()
                .observe(this, s ->
                        Toast.makeText(RestaurantDetailsActivity.this, s, Toast.LENGTH_SHORT)
                                .show());
    }

    private void workmateJoiningObserver() {
        mDetailsViewModel
                .getPlaceWorkmatesLiveData()
                .observe(this, bookings -> mWorkmateJoiningAdapter.setWorkmates(bookings));
    }

    private void isBookedRestaurantObserver() {
        mDetailsViewModel.getMyBookedRestaurantLiveData()
                .observe(this, this::showBookedButton);
    }

    private void restaurantRatingObserver() {
        mDetailsViewModel.getRatingLiveData()
                .observe(this, rating -> {
                    ratingBar.setRating(rating);

                    if(!isRated) return;
                    Toast.makeText(this, this.getString(R.string.rating_saved), Toast.LENGTH_LONG).show();

                    Intent data = new Intent();
                    data.putExtra(EXTRA_DATA_RATING_KEY, rating);
                    data.putExtra(EXTRA_DATA_PLACE_KEY, mPlaceId);
                    // Update all restaurant info
                    setResult(BOOKED_RESULT_CODE, data);
                });
    }

    private void listeners() {
        restaurantSuccessObserver();
        restaurantDetailsErrorObserver();
        workmateJoiningObserver();
        isBookedRestaurantObserver();
        restaurantRatingObserver();
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
        ProgressDialogFragment dialogFragment = ProgressDialogFragment.newInstance();
        dialogFragment.show(getSupportFragmentManager());

        disposable = RestaurantStreams
                .getRestaurantDetailsExtractedFlowable(this, mPlaceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(throwable -> dialogFragment.dismiss())
                .subscribe(restaurant -> {
                    mRestaurant = restaurant;
                    applyInfo();

                    dialogFragment.dismiss();
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
        Intent intent = new Intent(Intent.ACTION_DIAL,
                Uri.fromParts("tel", mRestaurant.getPhoneNumber(), null));
        startActivity(intent);
    }

    @OnClick(R.id.websiteBtn)
    void showWebSite() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mRestaurant.getWebsite()));
        startActivity(intent);
    }

    @OnClick(R.id.likeBtn)
    void showRatingDialog(){
        final ProgressDialogFragment dialogFragment = ProgressDialogFragment.newInstance();
        dialogFragment.show(getSupportFragmentManager());

        FireStoreUtils.getCurrentUserRating(mPlaceId,
                FireStoreUtils.getCurrentFirebaseUser().getUid())
                .addOnSuccessListener(documentSnapshot -> {
                    Rating rating = documentSnapshot.toObject(Rating.class);

                    String comment = "";
                    int stars = 0;

                    if(rating != null) {
                        comment = rating.comment;
                        stars = rating.stars;
                    }

                    RatingDialogFragment ratingDialogFragment =
                            RatingDialogFragment.newInstance(comment, stars);
                    ratingDialogFragment.show(getSupportFragmentManager());
                })
                .addOnCompleteListener(command -> dialogFragment.dismiss());
    }

    void confirmBooking() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.information)
                .setMessage(R.string.booking_confirmation)
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.yes_i_want,
                        (dialog, which) -> mDetailsViewModel.markRestaurantAsSelected(mRestaurant))
                .create()
                .show();
    }

    void confirmClosingSoonBooking() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.information)
                .setMessage(R.string.restaurant_is_closing_soon)
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.yes_i_want,
                        (dialog, which) -> mDetailsViewModel.markRestaurantAsSelected(mRestaurant))
                .create()
                .show();
    }

    void restaurantClosedInformation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.information)
                .setMessage(R.string.message_restaurant_is_closed)
                .setNegativeButton(R.string.ok, null)
                .create()
                .show();
    }

    void confirmCancelBooking() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.information)
                .setMessage(R.string.remove_booking_confirmation)
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.yes_remove_it,
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

        if(mRestaurant.isClosingSoon()) {
            confirmClosingSoonBooking();
        } else if(!mRestaurant.isOpenNow()) {
            restaurantClosedInformation();
        } else {
            confirmBooking();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(disposable != null && !disposable.isDisposed()){
            disposable.dispose();
        }
    }

    @Override
    public void onRatingFragmentInteraction(int stars, String comment) {
        Rating rating = new Rating();
        rating.stars = stars;
        rating.comment = comment;
        rating.placeId = mRestaurant.getPlaceId();
        rating.userId = FireStoreUtils.getCurrentFirebaseUser().getUid();

        //Save rating on firebase
        //Likes/placeId/users/userId + Object(Rating)
        mDetailsViewModel.saveRating(rating);

        isRated = true;
    }
}
