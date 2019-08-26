package com.apiman.go4lunch.ui.listview;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.apiman.go4lunch.models.Restaurant;

import java.util.ArrayList;
import java.util.List;

public class ListViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private MutableLiveData<List<Restaurant>> mRestaurantList;

    public ListViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");

        // Mock data
        mRestaurantList = new MutableLiveData<>();
        List<Restaurant> restaurants = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            restaurants.add(new Restaurant());
        }
        mRestaurantList.setValue(restaurants);
    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<List<Restaurant>> getRestaurantList(){
        return mRestaurantList;
    }
}