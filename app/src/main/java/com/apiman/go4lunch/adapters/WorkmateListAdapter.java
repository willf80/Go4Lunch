package com.apiman.go4lunch.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.apiman.go4lunch.R;
import com.apiman.go4lunch.models.Workmate;
import com.apiman.go4lunch.services.Utils;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class WorkmateListAdapter extends RecyclerView.Adapter<WorkmateListAdapter.WorkmateViewHolder> {

    private List<Workmate> mWorkmates;

    public WorkmateListAdapter(List<Workmate> workmates) {
        mWorkmates = workmates;
    }

    public void setWorkmates(List<Workmate> workmates) {
        mWorkmates = workmates;
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
        Workmate workmate = mWorkmates.get(position);
        holder.textView.setText(String.format("%s is eating !", Utils.shortName(workmate.displayName)));

        Picasso.get()
                .load(workmate.photo)
                .resize(64, 64)
                .centerCrop()
                .error(R.mipmap.ic_launcher)
                .into(holder.profileImageView);
    }

    @Override
    public int getItemCount() {
        return mWorkmates.size();
    }

    class WorkmateViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.textView)
        TextView textView;

        @BindView(R.id.profile_image)
        CircleImageView profileImageView;

        WorkmateViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
