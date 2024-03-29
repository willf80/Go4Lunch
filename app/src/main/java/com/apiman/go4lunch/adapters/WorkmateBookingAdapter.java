package com.apiman.go4lunch.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.apiman.go4lunch.R;
import com.apiman.go4lunch.models.Booking;
import com.apiman.go4lunch.models.Workmate;
import com.apiman.go4lunch.models.WorkmateBooking;
import com.apiman.go4lunch.helpers.Utils;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class WorkmateBookingAdapter extends RecyclerView.Adapter<WorkmateBookingAdapter.WorkmateViewHolder> {

    private List<WorkmateBooking> mWorkmateBookings;
    private DispatchListener mDispatchListener;

    public interface DispatchListener{
        void onClick(WorkmateBooking workmateBooking);
    }

    public WorkmateBookingAdapter(List<WorkmateBooking> workmateBookings, DispatchListener listener) {
        mWorkmateBookings = workmateBookings;
        mDispatchListener = listener;
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
            text = String.format(holder.context.getString(R.string.has_not_decided_yet), Utils.shortName(workmate.displayName));
            areBooked = true;
        }else{
            text = String.format(holder.context.getString(R.string.is_eating_at_restaurant),
                    Utils.shortName(workmate.displayName),
                    booking.restaurantName);
        }

        applyStyle(areBooked, holder.textView, holder.context);
        holder.textView.setText(text);
        holder.itemView.setOnClickListener(v -> mDispatchListener.onClick(workmateBooking));
        loadImage(workmate.photo, holder.profileImageView);
    }

    private void loadImage(String photoUrl, ImageView imageView) {
        Picasso.get()
                .load(photoUrl)
                .resize(64, 64)
                .centerCrop()
                .error(R.drawable.user_profile_black)
                .placeholder(R.drawable.user_profile_black)
                .into(imageView);
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
