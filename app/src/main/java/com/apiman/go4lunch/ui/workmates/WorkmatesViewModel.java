package com.apiman.go4lunch.ui.workmates;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.apiman.go4lunch.models.Workmate;

import java.util.ArrayList;
import java.util.List;

public class WorkmatesViewModel extends ViewModel {

    private MutableLiveData<List<Workmate>> mWorkmateList;
    private MutableLiveData<List<Workmate>> mWorkmateJoinList;

    public WorkmatesViewModel() {
        mWorkmateList = new MutableLiveData<>();
        mWorkmateJoinList = new MutableLiveData<>();

        List<Workmate> workmateList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            workmateList.add(new Workmate());
        }
        mWorkmateList.setValue(workmateList);

        List<Workmate> workmateJoinList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            workmateJoinList.add(new Workmate());
        }
        mWorkmateJoinList.setValue(workmateJoinList);
    }

    public LiveData<List<Workmate>> getWorkmateList() {
        return mWorkmateList;
    }

    public LiveData<List<Workmate>> getWorkmateJoinsList() {
        return mWorkmateJoinList;
    }
}