package com.apiman.go4lunch.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.apiman.go4lunch.models.Booking;
import com.apiman.go4lunch.services.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;

public class RestaurantDetailsViewModel extends ViewModel {
    private static final String COL_PATH_BOOKING = "booking";
    private static final String DOC_PATH_BOOK = "book";
    private static final String REF_WORKMATES = "workmates";

    private MutableLiveData<Void> mLiveData;
    private MutableLiveData<String> mErrorDispatcherLiveData;

    FirebaseUser mCurrentUser;
    FirebaseAuth mFirebaseAuth;
    FirebaseFirestore db;

    CollectionReference bookingRef;
    DocumentReference todayDocumentRef;

    String today = Utils.today();

    public RestaurantDetailsViewModel() {
        mLiveData = new MutableLiveData<>();
        mErrorDispatcherLiveData = new MutableLiveData<>();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        bookingRef = db.collection(COL_PATH_BOOKING);
        todayDocumentRef = bookingRef.document(today);
    }

    public LiveData<Void> updatedListener() {
        return mLiveData;
    }

    public LiveData<String> errorDispatcherLister() {
        return mErrorDispatcherLiveData;
    }

    public void markRestaurantAsSelected(String placeId) {
        String userRef = mCurrentUser.getUid();
        long timestamps = Calendar.getInstance().getTimeInMillis();

        // Get the collections
        CollectionReference restaurantCollectionRef = todayDocumentRef.collection(userRef);
//        restaurantCollectionRef.whereArrayContains("placeId", placeId)
//                .get()
//                .addOnCompleteListener(task -> {
//                    if(task.isSuccessful()){
//                        //mErrorDispatcherLiveData.setValue("Collection changed!");
//                        StringBuilder result = new StringBuilder();
//                        for (QueryDocumentSnapshot document : task.getResult()) {
//                            result.append(document.getId()).append(" => ").append(document.getData());
//                            result.append("\n");
//                        }
//
//                        mErrorDispatcherLiveData.setValue(result.toString());
//                    }else {
//                        mErrorDispatcherLiveData.setValue("Error");
//                    }
//
//                });
//        restaurantCollectionRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
//            if(queryDocumentSnapshots == null) return;
//            mErrorDispatcherLiveData.setValue("Collection changed!" + queryDocumentSnapshots.size());
//        });
//

        // Save
        Booking booking = new Booking(placeId, userRef, timestamps);

        restaurantCollectionRef.document(DOC_PATH_BOOK).set(booking)
            .addOnSuccessListener(aVoid -> {
                mLiveData.setValue(null);
            })
            .addOnFailureListener(e -> {
                mErrorDispatcherLiveData.setValue(e.getMessage());
            });
    }
}