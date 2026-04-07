package com.example.travelagencyapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelagencyapplication.adapters.ReviewAdapter;
import com.example.travelagencyapplication.api.ApiService;
import com.example.travelagencyapplication.api.RetrofitClient;
import com.example.travelagencyapplication.model.ReviewDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewsActivity extends AppCompatActivity {

    private ReviewAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reviews);

        recyclerView = findViewById(R.id.rvReviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        int packageTourId = getIntent().getIntExtra("packageTourId", -1);

        if (packageTourId != -1) {
            loadReviews(packageTourId);
        } else {
            Toast.makeText(this, "Greška pri učitavanju aranžmana", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadReviews(int packageTourId) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        apiService.getReviewsForPackage(packageTourId).enqueue(new Callback<List<ReviewDTO>>() {
            @Override
            public void onResponse(Call<List<ReviewDTO>> call, Response<List<ReviewDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ReviewDTO> reviews = response.body();

                    if (reviews.isEmpty()) {
                        Toast.makeText(ReviewsActivity.this, "Nema recenzija za ovaj aranžman", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        // Ovde postavljaš svoj adapter na RecyclerView
                        adapter = new ReviewAdapter(reviews);
                        //recyclerView.setLayoutManager(new LinearLayoutManager(this));
                        recyclerView.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ReviewDTO>> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
            }
        });
    }
}