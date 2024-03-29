package com.apiman.go4lunch.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.apiman.go4lunch.R;
import com.apiman.go4lunch.models.Restaurant;
import com.apiman.go4lunch.services.RestaurantStreams;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

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
        holder.ratingBar.setRating(restaurant.getRating());
        holder.workmatesTextView.setText(
                String.format(Locale.getDefault(), "(%d)", restaurant.getTotalWorkmates()));

        applyStyle(restaurant.isClosingSoon(), holder.statusTextView, holder.context);

        holder.itemView.setOnClickListener(v -> mOnDispatchListener.onItemClicked(restaurant));

        displayPhoto(holder.context, holder.photoImageView, restaurant.getPhotoReference());
    }

    private void applyStyle(boolean isClosingSoon, TextView textView, Context context) {
        if(isClosingSoon) {
            textView.setTextColor(ContextCompat.getColor(context, R.color.colorRed700));
            textView.setTypeface(null, Typeface.BOLD_ITALIC);
        }else {
            textView.setTextColor(ContextCompat.getColor(context, R.color.colorGrey600));
            textView.setTypeface(null, Typeface.ITALIC);
        }
    }

    private void displayPhoto(Context context, ImageView imageView, String photoReference){
        Picasso.get()
                .load(RestaurantStreams.getSmallPhotoUrl(context, photoReference))
                .resize(100, 100)
                .into(imageView);
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

        @BindView(R.id.workmatesTextView)
        TextView workmatesTextView;

        @BindView(R.id.imageView)
        ImageView photoImageView;

        @BindView(R.id.ratingBar)
        RatingBar ratingBar;

        Context context;

        RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            context = itemView.getContext();
        }
    }
}
