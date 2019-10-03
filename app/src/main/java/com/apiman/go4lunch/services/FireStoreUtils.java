package com.apiman.go4lunch.services;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FireStoreUtils {
    public static final String COL_PATH_BOOKING = "bookings";
    public static final String REF_WORKMATES = "workmates";
    public static final String FIELD_PLACE_ID = "placeId";

    public static CollectionReference getTodayBookingCollection(FirebaseFirestore db){
        CollectionReference bookingRef = db.collection(COL_PATH_BOOKING);
        String today = Utils.today();
        return bookingRef.document(today).collection(REF_WORKMATES);
    }
}
