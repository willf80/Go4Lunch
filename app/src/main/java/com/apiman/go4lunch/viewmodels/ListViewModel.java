package com.apiman.go4lunch.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.apiman.go4lunch.models.Restaurant;

import java.util.ArrayList;
import java.util.List;

public class ListViewModel extends ViewModel {

    private MutableLiveData<List<Restaurant>> mRestaurantList;

    public ListViewModel() {
        // Mock data
        mRestaurantList = new MutableLiveData<>();
        List<Restaurant> restaurants = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            restaurants.add(new Restaurant());
        }
        mRestaurantList.setValue(restaurants);
    }

    public LiveData<List<Restaurant>> getRestaurantList(){
        return mRestaurantList;
    }
}