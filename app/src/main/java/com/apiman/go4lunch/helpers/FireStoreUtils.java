package com.apiman.go4lunch.helpers;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.apiman.go4lunch.R;
import com.apiman.go4lunch.models.Rating;
import com.apiman.go4lunch.models.Workmate;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.reactivex.Flowable;

public class FireStoreUtils {
    private static final String COL_PATH_WORKMATES = "workmates";
    private static final String COL_PATH_BOOKINGS = "bookings";
    private static final String COL_PATH_BOOKS = "books";
    private static final String COL_PATH_RATINGS = "ratings";
    private static final String COL_PATH_RATING_USERS = "rating_users";

    public static final String FIELD_PHOTO = "photo";
    public static final String FIELD_PLACE_ID = "placeId";
    public static final String FIELD_USER = "user";

    public static CollectionReference getTodayBookingCollection(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference bookingRef = db.collection(COL_PATH_BOOKINGS);
        String today = Utils.today();
        return bookingRef.document(today).collection(COL_PATH_BOOKS);
    }

    public static GoogleSignInClient getGoogleSignInClient(Context context) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        return GoogleSignIn.getClient(context, gso);
    }

    public static TwitterConfig getTwitterConfig(Context context) {
        TwitterAuthConfig authConfig = new TwitterAuthConfig(
                context.getString(R.string.twitter_consumer_key),
                context.getString(R.string.twitter_consumer_secret));

        return new TwitterConfig.Builder(context).twitterAuthConfig(authConfig).build();
    }

    public static CollectionReference getWorkmatesCollection() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(COL_PATH_WORKMATES);
    }

    public static CollectionReference getRestaurantRatingScore(String placeId){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(COL_PATH_RATINGS).document(placeId).collection(COL_PATH_RATING_USERS);
    }

    public static FirebaseUser getCurrentFirebaseUser() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        return firebaseAuth.getCurrentUser();
    }

    public static Task<Void> saveUser(FirebaseUser firebaseUser) {
        if(firebaseUser == null) return null;

        Workmate workmate = getCurrentWorkmateUser(firebaseUser);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference users = db.collection(COL_PATH_WORKMATES);

        return users.document(workmate.uuid).set(workmate);
    }

    public static Workmate getCurrentWorkmateUser(FirebaseUser firebaseUser){
        if(firebaseUser == null) return null;

        Uri uri = firebaseUser.getPhotoUrl();
        String url = null;
        if(uri != null){
            url = uri.toString();
        }

        String displayName = firebaseUser.getDisplayName();
        if(displayName == null || displayName.isEmpty()) {
            displayName = firebaseUser.getEmail();
        }

        return new Workmate(
                firebaseUser.getUid(),
                displayName,
                firebaseUser.getEmail(),
                url,
                new Date());
    }

    public static @NonNull Task<DocumentSnapshot> getWorkmateBookOfDay(String workmateId) {
        return getTodayBookingCollection().document(workmateId)
                .get();
    }

    public static QuerySnapshot getTodayBookingAwait() throws ExecutionException, InterruptedException {
        return Tasks.await(getTodayBookingCollection().get());
    }

    public static Task<Void> removeBooking() {
        String userId = getCurrentFirebaseUser().getUid();
        return getTodayBookingCollection().document(userId).delete();
    }

    public static Task<Void> saveRating(Rating rating) {
        return getRestaurantRatingScore(rating.placeId)
                .document(rating.userId)
                .set(rating);
    }

    public static Float averageRating(QuerySnapshot querySnapshot) {
        List<DocumentSnapshot> documentSnapshots = querySnapshot.getDocuments();
        int size = documentSnapshots.size();
        if(size == 0) {
            size = 1;
        }
        final float total = size;

        return Flowable.fromIterable(documentSnapshots)
                .reduce(0f,(rate, documentSnapshot) -> {
                    Rating rating = documentSnapshot.toObject(Rating.class);
                    assert rating != null;
                    rate += rating.stars;
                    return rate;
                })
                .map(sum -> sum / total)
                .blockingGet();
    }

    public static Task<DocumentSnapshot> getCurrentUserRating(String placeId, String userId) {
        return getRestaurantRatingScore(placeId).document(userId).get();
    }
}
