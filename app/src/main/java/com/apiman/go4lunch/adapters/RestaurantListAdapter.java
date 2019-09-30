package com.apiman.go4lunch.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.apiman.go4lunch.R;
import com.apiman.go4lunch.models.Restaurant;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

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

        holder.statusTextView.setText(restaurant.getStatus());
        holder.nameTextView.setText(restaurant.getName());
        holder.addressTextView.setText(restaurant.getPlaceId());
        holder.distanceTextView.setText(restaurant.getDistanceWithSuffix());
        holder.addressTextView.setText(restaurant.getAddress());

        holder.itemView.setOnClickListener(v -> mOnDispatchListener.onItemClicked(restaurant));
    }

    @Override
    public int getItemCount() {
        return mRestaurants.size();
    }

    class RestaurantViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.nameTextView)
        TextView nameTextView;

        @BindView(R.id.addressTextView)
        TextView addressTextView;

        @BindView(R.id.distanceTextView)
        TextView distanceTextView;

        @BindView(R.id.textView5)
        TextView statusTextView;

        RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
