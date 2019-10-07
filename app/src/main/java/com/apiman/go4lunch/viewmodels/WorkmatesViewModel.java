package com.apiman.go4lunch.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.apiman.go4lunch.models.Booking;
import com.apiman.go4lunch.models.Workmate;
import com.apiman.go4lunch.models.WorkmateBooking;
import com.apiman.go4lunch.services.FireStoreUtils;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class WorkmatesViewModel extends ViewModel {

    private MutableLiveData<List<WorkmateBooking>> mWorkmateList;
    private Disposable disposable;

    public WorkmatesViewModel() {
        mWorkmateList = new MutableLiveData<>();
    }

    public LiveData<List<WorkmateBooking>> getWorkmatesLiveData() {
        return mWorkmateList;
    }

    public void loadWorkmates() {
        FireStoreUtils.getWorkmatesCollection()
            .get()
            .addOnSuccessListener(this::fetchWorkmatesData)
            .addOnFailureListener(e -> {

            });
    }

    private void fetchWorkmatesData(QuerySnapshot query){
        List<Workmate> workmateList = new ArrayList<>();
        for (DocumentSnapshot doc : query.getDocuments()) {
            Workmate workmate = doc.toObject(Workmate.class);
            if(workmate == null) continue;

            workmateList.add(workmate);
        }

        disposable = Observable.fromIterable(workmateList)
                .reduce(new ArrayList<WorkmateBooking>(), (workmateBookingList, workmate) -> {
                    // Async await
                    DocumentSnapshot snapshot = Tasks.await(FireStoreUtils.getWorkmateBookOfDay(workmate.uuid));

                    WorkmateBooking workmateBooking = new WorkmateBooking(workmate);
                    if(snapshot != null) {
                        Booking booking = snapshot.toObject(Booking.class);
                        if(booking != null) {
                            workmateBooking.booking = booking;
                        }
                    }

                    workmateBookingList.add(workmateBooking);

                    return workmateBookingList;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(workmateBookings -> mWorkmateList.setValue(workmateBookings));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if(disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}