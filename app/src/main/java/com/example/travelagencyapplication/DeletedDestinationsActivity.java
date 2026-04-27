package com.example.travelagencyapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelagencyapplication.adapters.DestinationAdapter;
import com.example.travelagencyapplication.api.ApiService;
import com.example.travelagencyapplication.api.RetrofitClient;
import com.example.travelagencyapplication.model.TourPackageCategory;
import com.example.travelagencyapplication.model.TourPackageDTO;
import com.example.travelagencyapplication.model.User;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeletedDestinationsActivity extends AppCompatActivity {

    RecyclerView rvDestinations;
    DestinationAdapter adapter;

    List<TourPackageCategory> categories = new ArrayList<>();
    ApiService apiService;

    Spinner spinner;
    Button btnBackToActive, btnEditProfile;
    TextView tvWelcome, tvEmail, tvLastname, tvFirstname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);

        rvDestinations = findViewById(R.id.rvDestinations);
        spinner        = findViewById(R.id.spinnerCategories);
        rvDestinations.setLayoutManager(new GridLayoutManager(this, 2));
        btnBackToActive = findViewById(R.id.btnBackToActive);
        tvWelcome       = findViewById(R.id.tvWelcome);
        tvEmail         = findViewById(R.id.tvEmail);
        tvFirstname     = findViewById(R.id.tvFirstname);
        tvLastname      = findViewById(R.id.tvLastname);
        apiService      = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        if (btnEditProfile != null) {
            btnEditProfile.setVisibility(View.GONE); // Sakrij dugme za izmenu u kanti
        }
        btnBackToActive.setVisibility(View.VISIBLE);
        btnBackToActive.setOnClickListener(v -> {
            finish(); // Zatvara kantu i vraća na UserPageActivity
        });

        SharedPreferences sp = getSharedPreferences("TravelAgencyApplication", MODE_PRIVATE);
        String currentUser = sp.getString("userId", "-1");

        if (!currentUser.equals("-1")) {
            loadUserInfo(currentUser);
        }

        loadCategories();
        loadDeletedPackagesFromServer((long) -1);
    }

    public void loadCategories() {
        apiService.getAllCategories().enqueue(new Callback<List<TourPackageCategory>>() {
            @Override
            public void onResponse(Call<List<TourPackageCategory>> call, Response<List<TourPackageCategory>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body();
                    List<String> categoriesNames = new ArrayList<>();
                    categoriesNames.add("Sve kategorije (Obrisano)");

                    for (TourPackageCategory c : categories) {
                        categoriesNames.add(c.getName());
                    }

                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                            DeletedDestinationsActivity.this,
                            android.R.layout.simple_spinner_item,
                            categoriesNames
                    );
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(spinnerAdapter);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (position == 0) {
                                loadDeletedPackagesFromServer((long) -1);
                            } else {
                                Long categoryId = (long) categories.get(position - 1).getId();
                                loadDeletedPackagesFromServer(categoryId);
                            }
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                }
            }

            @Override
            public void onFailure(Call<List<TourPackageCategory>> call, Throwable t) {
                Log.e("API_ERROR", "Greška kategorije: " + t.getMessage());
            }
        });
    }
    private void loadDeletedPackagesFromServer(Long categoryId) {
        apiService.getTourPackages("deleted", categoryId).enqueue(new Callback<List<TourPackageDTO>>() {
            @Override
            public void onResponse(Call<List<TourPackageDTO>> call, Response<List<TourPackageDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TourPackageDTO> packages = response.body();

                    adapter = new DestinationAdapter(packages, "ROLE_ADMIN");
                    rvDestinations.setAdapter(adapter);

                    if(packages.isEmpty()) {
                        Toast.makeText(DeletedDestinationsActivity.this, "Nema obrisanih stavki u ovoj kategoriji", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<TourPackageDTO>> call, Throwable t) {
                Log.e("API_ERROR", "Greška: " + t.getMessage());
            }
        });
    }
    private void loadUserInfo(String userId) {
        long id = Long.parseLong(userId);
        apiService.getUserByUserId(id).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    tvWelcome.setText("Obrisane destinacije");
                    tvEmail.setText("Admin: " + user.getEmail());
                    tvFirstname.setText("Ime: " + user.getFirstName());
                    tvLastname.setText("Prezime: " + user.getLastName());
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
            }
        });
    }
}