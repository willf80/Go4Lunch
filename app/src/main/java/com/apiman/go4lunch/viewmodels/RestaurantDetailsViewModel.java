package com.apiman.go4lunch.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.apiman.go4lunch.models.Restaurant;
import com.apiman.go4lunch.models.Workmate;
import com.apiman.go4lunch.services.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.realm.Realm;

import static com.apiman.go4lunch.services.FireStoreUtils.COL_PATH_BOOKING;
import static com.apiman.go4lunch.services.FireStoreUtils.FIELD_PLACE_ID;
import static com.apiman.go4lunch.services.FireStoreUtils.REF_WORKMATES;

public class RestaurantDetailsViewModel extends ViewModel {

    private MutableLiveData<String> mSuccessLiveData;
    private MutableLiveData<String> mErrorDispatcherLiveData;
    private MutableLiveData<List<Workmate>> mAllWorkmatesLiveData;
    private MutableLiveData<List<Workmate>> mPlaceWorkmatesLiveData;

    private FirebaseUser mCurrentUser;
    private CollectionReference todayWorkmatesRef;

    public RestaurantDetailsViewModel() {
        mSuccessLiveData = new MutableLiveData<>();
        mErrorDispatcherLiveData = new MutableLiveData<>();
        mAllWorkmatesLiveData = new MutableLiveData<>();
        mPlaceWorkmatesLiveData = new MutableLiveData<>();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = firebaseAuth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference bookingRef = db.collection(COL_PATH_BOOKING);
        String today = Utils.today();
        todayWorkmatesRef = bookingRef.document(today).collection(REF_WORKMATES);
    }

    public LiveData<String> updatedListener() {
        return mSuccessLiveData;
    }

    public LiveData<String> errorDispatcherLister() {
        return mErrorDispatcherLiveData;
    }

    public LiveData<List<Workmate>> getAllWorkmatesLiveData() {
        return mAllWorkmatesLiveData;
    }

    public LiveData<List<Workmate>> getPlaceWorkmatesLiveData() {
        return mPlaceWorkmatesLiveData;
    }

    public void markRestaurantAsSelected(Restaurant restaurant) {
        String userRef = mCurrentUser.getUid();

        // Get the collections
        Workmate workmate = new Workmate();
        workmate.displayName = mCurrentUser.getDisplayName();
        workmate.restaurantAddress = restaurant.getAddress();
        workmate.restaurantName = restaurant.getName();
        workmate.timestamps = Calendar.getInstance().getTime();
        workmate.userId = mCurrentUser.getUid();
        workmate.placeId = restaurant.getPlaceId();

        todayWorkmatesRef.document(userRef).set(workmate)
            .addOnSuccessListener(aVoid ->mSuccessLiveData.setValue(restaurant.getPlaceId()))
            .addOnFailureListener(e -> mErrorDispatcherLiveData.setValue(e.getMessage()));
    }

    public void getWorkmatesOfRestaurant(String placeId) {
        todayWorkmatesRef.whereEqualTo(FIELD_PLACE_ID, placeId)
            .get()
            .addOnSuccessListener(query -> {
                List<Workmate> workmateList = new ArrayList<>();
                for (DocumentSnapshot doc : query.getDocuments()) {
                    Workmate workmate = doc.toObject(Workmate.class);
                    if(workmate == null) continue;
                    workmateList.add(workmate);
                }

                mPlaceWorkmatesLiveData.setValue(workmateList);
            })
            .addOnFailureListener(e -> {
                mErrorDispatcherLiveData.setValue(e.getMessage());
            });
    }

    public void updateBookingsOfDay() {
        todayWorkmatesRef.get()
            .addOnSuccessListener(query -> {
                for (DocumentSnapshot doc : query.getDocuments()) {
                    Workmate workmate = doc.toObject(Workmate.class);
                    if(workmate == null) continue;

                    updateRealmDatabase(workmate);
                }

                //TODO : Refresh maps and list view
//                mAllWorkmatesLiveData
            })
            .addOnFailureListener(e -> {
                mErrorDispatcherLiveData.setValue(e.getMessage());
            });
    }

    private void updateRealmDatabase(@NonNull Workmate workmate){
        Realm realm = Realm.getDefaultInstance();

        Restaurant restaurant = realm.where(Restaurant.class)
                .equalTo("placeId", workmate.placeId)
                .findFirst();

        if(restaurant == null) return;

        realm.beginTransaction();
        restaurant.setBook(true);
        realm.commitTransaction();
    }
}