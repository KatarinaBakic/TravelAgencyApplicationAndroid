package com.example.travelagencyapplication;

import android.content.SharedPreferences;
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

import com.example.travelagencyapplication.adapters.CategoryAdapter;
import com.example.travelagencyapplication.api.ApiService;
import com.example.travelagencyapplication.api.RetrofitClient;
import com.example.travelagencyapplication.model.CategoryDTO;
import com.example.travelagencyapplication.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AvailableDestinationsActivity extends AppCompatActivity {

    private RecyclerView rvCategories;
    private CategoryAdapter adapter;
    private User loggedInUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_destinations);

        rvCategories = findViewById(R.id.rvCategories);
        rvCategories.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences sp = getSharedPreferences("TravelAgencyApplication", MODE_PRIVATE);
        String userId = sp.getString("userId", "-1");

        Log.e("USERID" , userId);
        if (userId.equals("-1")) {
            fetchCategories();
        } else {
            loadUserInfo(userId);
        }
        //loadUserInfo(userId);
    }
    private void loadUserInfo(String userId) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        apiService.getUserByUserId(Long.parseLong(userId)).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    loggedInUser = response.body();
                    fetchCategories();
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                fetchCategories();
            }
        });
    }

    private void fetchCategories() {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        apiService.getAvailableDestinations().enqueue(new Callback<List<CategoryDTO>>() {
            @Override
            public void onResponse(Call<List<CategoryDTO>> call, Response<List<CategoryDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String role = (loggedInUser != null) ? loggedInUser.getRole() : "ROLE_USER";
                    adapter = new CategoryAdapter(response.body(), AvailableDestinationsActivity.this, role);
                    rvCategories.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<CategoryDTO>> call, Throwable t) {
                Toast.makeText(AvailableDestinationsActivity.this, "Greška pri učitavanju", Toast.LENGTH_SHORT).show();
            }
        });
    }
}