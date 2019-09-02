package com.apiman.go4lunch.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.apiman.go4lunch.R;
import com.apiman.go4lunch.models.Restaurant;

import java.util.List;

public class RestaurantListAdapter extends RecyclerView.Adapter<RestaurantListAdapter.RestaurantViewHolder> {

    private List<Restaurant> mRestaurants;
    private OnDispatchListener mOnDispatchListener;

    public interface OnDispatchListener{
        void onItemClicked(Restaurant restaurant);
    }

    public RestaurantListAdapter(List<Restaurant> restaurants, OnDispatchListener onDispatchListener) {
        mRestaurants = restaurants;
        mOnDispatchListener = onDispatchListener;
    }

    public void setRestaurants(List<Restaurant> restaurants) {
        mRestaurants = restaurants;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.restaurant_list_item_view, parent, false);

        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        final Restaurant restaurant = mRestaurants.get(position);

        holder.itemView.setOnClickListener(v -> mOnDispatchListener.onItemClicked(restaurant));
    }

    @Override
    public int getItemCount() {
        return mRestaurants.size();
    }

    class RestaurantViewHolder extends RecyclerView.ViewHolder{

        RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
