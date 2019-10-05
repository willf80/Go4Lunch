package com.apiman.go4lunch.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.apiman.go4lunch.models.Workmate;
import com.apiman.go4lunch.services.FireStoreUtils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class WorkmatesViewModel extends ViewModel {

    private MutableLiveData<List<Workmate>> mWorkmateList;

    public WorkmatesViewModel() {
        mWorkmateList = new MutableLiveData<>();
    }

    public LiveData<List<Workmate>> getWorkmatesLiveData() {
        return mWorkmateList;
    }

    public void loadWorkmates() {
        FireStoreUtils.getWorkmatesCollection()
            .get()
            .addOnSuccessListener(query -> fetchWorkmatesData(query))
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

        mWorkmateList.setValue(workmateList);
    }
}