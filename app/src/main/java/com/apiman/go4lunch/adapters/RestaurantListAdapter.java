package com.apiman.go4lunch.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.apiman.go4lunch.R;
import com.apiman.go4lunch.models.OpenCloseHour;
import com.apiman.go4lunch.models.Restaurant;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmQuery;

public class RestaurantListAdapter extends RecyclerView.Adapter<RestaurantListAdapter.RestaurantViewHolder> {

    private List<Restaurant> mRestaurants;
    private OnDispatchListener mOnDispatchListener;
    Realm mRealm;

    public interface OnDispatchListener{
        void onItemClicked(Restaurant restaurant);
    }

    public RestaurantListAdapter(List<Restaurant> restaurants, OnDispatchListener onDispatchListener) {
        mRestaurants = restaurants;
        mOnDispatchListener = onDispatchListener;
        mRealm = Realm.getDefaultInstance();
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

//        RealmQuery<OpenCloseHour> realmQuery = mRealm.where(OpenCloseHour.class)
//                .equalTo("placeId", restaurant.getPlaceId());

        if(restaurant.isOpenNow()) {
            holder.statusTextView.setText("Open until " + restaurant.getTimeText());
        }else {
            holder.statusTextView.setText("Closed " + restaurant.getTimeText());
        }

        holder.nameTextView.setText(restaurant.getName());
        holder.addressTextView.setText(restaurant.getPlaceId());


        String prefix = "m";
        int distance = restaurant.getDistance();
        if(distance > 1000) {
            prefix = "km";
            distance = distance / 1000;
        }

        String distanceText = String.format(Locale.getDefault(), "%d%s", distance, prefix );
        holder.distanceTextView.setText(distanceText);
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
