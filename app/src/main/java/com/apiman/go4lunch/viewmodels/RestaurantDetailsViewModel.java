package com.apiman.go4lunch.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.apiman.go4lunch.models.Booking;
import com.apiman.go4lunch.models.Restaurant;
import com.apiman.go4lunch.models.Workmate;
import com.apiman.go4lunch.services.FireStoreUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.realm.Realm;

import static com.apiman.go4lunch.services.FireStoreUtils.FIELD_PLACE_ID;
import static com.apiman.go4lunch.services.FireStoreUtils.FIELD_USER;

public class RestaurantDetailsViewModel extends ViewModel {

    private MutableLiveData<String> mSuccessLiveData;
    private MutableLiveData<String> mErrorDispatcherLiveData;
    private MutableLiveData<List<Booking>> mAllBookingsLiveData;
    private MutableLiveData<List<Workmate>> mPlaceWorkmatesLiveData;

    private FirebaseUser mCurrentUser;
    private CollectionReference todayBookingsRef;

    public RestaurantDetailsViewModel() {
        mSuccessLiveData = new MutableLiveData<>();
        mErrorDispatcherLiveData = new MutableLiveData<>();
        mAllBookingsLiveData = new MutableLiveData<>();
        mPlaceWorkmatesLiveData = new MutableLiveData<>();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = firebaseAuth.getCurrentUser();

        todayBookingsRef = FireStoreUtils.getTodayBookingCollection();
    }

    public LiveData<String> updatedListener() {
        return mSuccessLiveData;
    }

    public LiveData<String> errorDispatcherLister() {
        return mErrorDispatcherLiveData;
    }

    public LiveData<List<Booking>> getAllBookingsLiveData() {
        return mAllBookingsLiveData;
    }

    public LiveData<List<Workmate>> getPlaceWorkmatesLiveData() {
        return mPlaceWorkmatesLiveData;
    }

    public void markRestaurantAsSelected(Restaurant restaurant) {
        String userRef = mCurrentUser.getUid();

        // Get the collections
        Booking booking = new Booking();
        booking.restaurantAddress = restaurant.getAddress();
        booking.restaurantName = restaurant.getName();
        booking.timestamps = Calendar.getInstance().getTime();
        booking.placeId = restaurant.getPlaceId();
        booking.user = FireStoreUtils.getCurrentWorkmateUser(FirebaseAuth.getInstance().getCurrentUser());

        todayBookingsRef.document(userRef).set(booking)
            .addOnSuccessListener(aVoid ->mSuccessLiveData.setValue(restaurant.getPlaceId()))
            .addOnFailureListener(e -> mErrorDispatcherLiveData.setValue(e.getMessage()));
    }

    public void getWorkmatesOfRestaurant(String placeId) {
        todayBookingsRef.whereEqualTo(FIELD_PLACE_ID, placeId)
            .get()
            .addOnSuccessListener(query -> {
                List<Workmate> workmates = new ArrayList<>();
                for (DocumentSnapshot doc : query.getDocuments()) {
                    Workmate workmate = doc.get(FIELD_USER, Workmate.class);

                    if(workmate == null) continue;
                    workmates.add(workmate);
                }

                mPlaceWorkmatesLiveData.setValue(workmates);
            })
            .addOnFailureListener(e -> {
                //Error
                mErrorDispatcherLiveData.setValue(e.getMessage());
            });
    }

    public void updateBookingsOfDay() {
        todayBookingsRef.get()
            .addOnSuccessListener(query -> {
                for (DocumentSnapshot doc : query.getDocuments()) {
                    Booking booking = doc.toObject(Booking.class);
                    if(booking == null) continue;

                    updateRealmDatabase(booking.placeId);
                }

                //TODO : Refresh maps and list view
//                mAllBookingsLiveData
            })
            .addOnFailureListener(e -> {
                //Error
                mErrorDispatcherLiveData.setValue(e.getMessage());
            });
    }

    private void updateRealmDatabase(@NonNull String placeId){
        Realm realm = Realm.getDefaultInstance();

        Restaurant restaurant = realm.where(Restaurant.class)
                .equalTo(FIELD_PLACE_ID, placeId)
                .findFirst();

        if(restaurant == null) return;

        realm.beginTransaction();
        restaurant.setBook(true);
        realm.commitTransaction();

        realm.close();
    }
}