package com.example.travelagencyapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelagencyapplication.R;
import com.example.travelagencyapplication.model.ReviewDTO;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private List<ReviewDTO> reviewList;
    public ReviewAdapter(List<ReviewDTO> reviewList) {
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ReviewDTO review = reviewList.get(position);
        holder.tvUser.setText("Korisnik ID: " + review.getUserId());
        holder.tvComment.setText(review.getReview());
        holder.rbStars.setRating((float) review.getRating());
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser, tvComment;
        RatingBar rbStars;
        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUser    = itemView.findViewById(R.id.tvReviewUser);
            tvComment = itemView.findViewById(R.id.tvReviewComment);
            rbStars   = itemView.findViewById(R.id.rbReviewStars);
        }
    }
}