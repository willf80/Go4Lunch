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
import androidx.fragment.app.FragmentManager;

import com.apiman.go4lunch.R;


public class RatingDialogFragment extends DialogFragment {
    private static final String EXTRA_COMMENT_KEY = "comment";
    private static final String EXTRA_STARS_KEY = "stars";

    private OnRatingFragmentInteractionListener mListener;
    private int stars;
    private String comment;

    public static RatingDialogFragment newInstance(String comment, int stars) {
        RatingDialogFragment fragment = new RatingDialogFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_COMMENT_KEY, comment);
        args.putInt(EXTRA_STARS_KEY, stars);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null) {
            stars = getArguments().getInt(EXTRA_STARS_KEY, 0);
            comment = getArguments().getString(EXTRA_COMMENT_KEY, "");
        }
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

        commentEditText.setText(comment);
        ratingBar.setRating(stars);

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

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, RatingDialogFragment.class.getSimpleName());
    }

    public interface OnRatingFragmentInteractionListener {
        void onRatingFragmentInteraction(int rating, String comment);
    }
}
