package com.apiman.go4lunch.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.apiman.go4lunch.models.Booking;
import com.apiman.go4lunch.models.Restaurant;
import com.apiman.go4lunch.models.Workmate;
import com.apiman.go4lunch.services.FireStoreUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import static com.apiman.go4lunch.services.FireStoreUtils.FIELD_PLACE_ID;
import static com.apiman.go4lunch.services.FireStoreUtils.FIELD_USER;

public class RestaurantDetailsViewModel extends ViewModel {

    private MutableLiveData<String> mSuccessLiveData;
    private MutableLiveData<String> mErrorDispatcherLiveData;
    private MutableLiveData<List<Workmate>> mPlaceWorkmatesLiveData;
    private MutableLiveData<Boolean> mMyBookedRestaurantLiveData;

    private CollectionReference todayBookingsRef;

    public RestaurantDetailsViewModel() {
        mSuccessLiveData = new MutableLiveData<>();
        mErrorDispatcherLiveData = new MutableLiveData<>();
        mPlaceWorkmatesLiveData = new MutableLiveData<>();
        mMyBookedRestaurantLiveData = new MutableLiveData<>();

        todayBookingsRef = FireStoreUtils.getTodayBookingCollection();
    }

    public LiveData<String> updatedListener() {
        return mSuccessLiveData;
    }

    public LiveData<String> errorDispatcherLister() {
        return mErrorDispatcherLiveData;
    }

    public LiveData<List<Workmate>> getPlaceWorkmatesLiveData() {
        return mPlaceWorkmatesLiveData;
    }

    public LiveData<Boolean> getMyBookedRestaurantLiveData() {
        return mMyBookedRestaurantLiveData;
    }

    public void markRestaurantAsSelected(Restaurant restaurant) {
        String userRef = FireStoreUtils.getCurrentFirebaseUser().getUid();

        // Get the collections
        Booking booking = new Booking();
        booking.restaurantAddress = restaurant.getAddress();
        booking.restaurantName = restaurant.getName();
        booking.timestamps = Calendar.getInstance().getTime();
        booking.placeId = restaurant.getPlaceId();
        booking.user = FireStoreUtils.getCurrentWorkmateUser(FirebaseAuth.getInstance().getCurrentUser());

        todayBookingsRef
            .document(userRef)
            .set(booking)
            .addOnSuccessListener(aVoid -> mSuccessLiveData.setValue(restaurant.getPlaceId()))
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

    public void checkIfRestaurantIsBookedByCurrentUser(String currentPlaceId){
        String userId = FireStoreUtils.getCurrentFirebaseUser().getUid();
        FireStoreUtils
                .getTodayBookingCollection()
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String placeId = documentSnapshot.get(FIELD_PLACE_ID, String.class);
                    boolean result = (placeId != null && Objects.equals(placeId, currentPlaceId));
                    mMyBookedRestaurantLiveData.setValue(result);
                });
    }

    public void removeBooking(String placeId) {
        FireStoreUtils.removeBooking()
                .addOnSuccessListener(aVoid -> {
                    checkIfRestaurantIsBookedByCurrentUser(placeId);
                    getWorkmatesOfRestaurant(placeId);
                });
    }
}