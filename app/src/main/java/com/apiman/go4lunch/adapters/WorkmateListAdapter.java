package com.apiman.go4lunch.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.apiman.go4lunch.R;
import com.apiman.go4lunch.models.Workmate;

import java.util.List;

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

    }

    @Override
    public int getItemCount() {
        return mWorkmates.size();
    }

    class WorkmateViewHolder extends RecyclerView.ViewHolder{

        public WorkmateViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
