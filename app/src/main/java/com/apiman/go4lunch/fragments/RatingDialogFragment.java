package com.apiman.go4lunch.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.apiman.go4lunch.R;


public class RatingDialogFragment extends DialogFragment {

    private OnRatingFragmentInteractionListener mListener;

    public static RatingDialogFragment newInstance() {
        RatingDialogFragment fragment = new RatingDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        dialog.setContentView(createView());
        Window window = dialog.getWindow();
        if(window != null){
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        return dialog;
    }

    private View createView() {
        @SuppressLint("InflateParams")
        View view = LayoutInflater
                .from(getContext())
                .inflate(R.layout.fragment_rating_dialog, null);

        Button cancelBtn = view.findViewById(R.id.cancelBtn);
        Button okBtn = view.findViewById(R.id.okBtn);
        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        EditText commentEditText = view.findViewById(R.id.commentEditText);

        cancelBtn.setOnClickListener(v -> dismiss());
        okBtn.setOnClickListener(v -> {
            int rating = (int) ratingBar.getRating();
            String comment = commentEditText.getText().toString();
            onButtonOkPressed(rating, comment);
        });

        return view;
    }

    private void onButtonOkPressed(int rating, String comment) {
        if (mListener == null) return;
        if(rating <= 0){
            Toast.makeText(
                    getContext(),
                    getString(R.string.warning_select_number_of_star),
                    Toast.LENGTH_LONG).show();
            return;
        }
        dismiss();
        mListener.onRatingFragmentInteraction(rating, comment);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnRatingFragmentInteractionListener) {
            mListener = (OnRatingFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnRatingFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnRatingFragmentInteractionListener {
        void onRatingFragmentInteraction(int rating, String comment);
    }
}
