package com.apiman.go4lunch.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.apiman.go4lunch.R;
import com.apiman.go4lunch.models.Booking;
import com.apiman.go4lunch.models.Workmate;
import com.apiman.go4lunch.models.WorkmateBooking;
import com.apiman.go4lunch.services.Utils;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class WorkmateBookingAdapter extends RecyclerView.Adapter<WorkmateBookingAdapter.WorkmateViewHolder> {

    private List<WorkmateBooking> mWorkmateBookings;

    public WorkmateBookingAdapter(List<WorkmateBooking> workmateBookings) {
        mWorkmateBookings = workmateBookings;
    }

    public void setWorkmateBookings(List<WorkmateBooking> workmateBookings) {
        mWorkmateBookings = workmateBookings;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WorkmateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.workmate_list_item_view, parent, false);

        return new WorkmateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkmateViewHolder holder, int position) {
        WorkmateBooking workmateBooking = mWorkmateBookings.get(position);
        Workmate workmate = workmateBooking.workmate;
        Booking booking = workmateBooking.booking;

        String text;
        boolean areBooked = false;
        if(booking == null) {
            text = String.format("%s hasn't decided yet", Utils.shortName(workmate.displayName));
            areBooked = true;
        }else{
            text = String.format("%s is eating _____ (%s)",
                    Utils.shortName(workmate.displayName),
                    booking.restaurantName);
        }

        applyStyle(areBooked, holder.textView, holder.context);

        holder.textView.setText(text);

        Picasso.get()
                .load(workmate.photo)
                .resize(64, 64)
                .centerCrop()
                .error(R.mipmap.ic_launcher)
                .into(holder.profileImageView);
    }

    private void applyStyle(boolean areBooked, TextView textView, Context context) {
        if(areBooked) {
            textView.setTextColor(ContextCompat.getColor(context, R.color.colorGrey600));
            textView.setTypeface(null, Typeface.ITALIC);
        }else {
            textView.setTextColor(ContextCompat.getColor(context, android.R.color.black));
            textView.setTypeface(null, Typeface.NORMAL);
        }
    }

    @Override
    public int getItemCount() {
        return mWorkmateBookings.size();
    }

    class WorkmateViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.textView)
        TextView textView;

        @BindView(R.id.profile_image)
        CircleImageView profileImageView;

        Context context;

        WorkmateViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            context = itemView.getContext();
        }
    }
}
