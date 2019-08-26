package com.apiman.go4lunch.ui.workmates;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.apiman.go4lunch.models.Workmate;

import java.util.ArrayList;
import java.util.List;

public class WorkmatesViewModel extends ViewModel {

    private MutableLiveData<List<Workmate>> mWorkmateList;

    public WorkmatesViewModel() {
        mWorkmateList = new MutableLiveData<>();

        List<Workmate> workmateList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            workmateList.add(new Workmate());
        }

        mWorkmateList.setValue(workmateList);
    }

    public LiveData<List<Workmate>> getWorkmateList() {
        return mWorkmateList;
    }
}