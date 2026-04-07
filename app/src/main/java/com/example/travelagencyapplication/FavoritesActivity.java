package com.example.travelagencyapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;

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
import com.example.travelagencyapplication.model.TourPackage;
import com.example.travelagencyapplication.model.TourPackageDTO;
import com.example.travelagencyapplication.utils.CategoryUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritesActivity extends AppCompatActivity {

    RecyclerView rvFavorites;
    DestinationAdapter adapter;
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favorites);

        rvFavorites = findViewById(R.id.rvFavorites);
        rvFavorites.setLayoutManager(new GridLayoutManager(this, 2));

        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        // Uzmi ID ulogovanog korisnika iz SharedPreferences
        SharedPreferences sp = getSharedPreferences("TravelAgencyApplication", MODE_PRIVATE);
        String userIdStr = sp.getString("userId", "-1");

        if (!userIdStr.equals("-1")) {
            loadFavorites(Long.parseLong(userIdStr));

            Spinner spinner = findViewById(R.id.spinnerFavCategories);
            CategoryUtils.setupCategorySpinner(this, apiService, spinner, categoryId -> {
                // Pozivaš svoju metodu za učitavanje favorita sa tim ID-jem
                loadFavorites(categoryId);
            });
        }
    }
    private void loadFavorites(Long catId) {

        // Uzmi userId iz SharedPreferences (isto kao u UserPageActivity)
        SharedPreferences sp = getSharedPreferences("TravelAgencyApplication", MODE_PRIVATE);
        long userId = Long.parseLong(sp.getString("userId", "-1"));

        apiService.getFavoritePackages(userId, catId).enqueue(new Callback<List<TourPackageDTO>>() {

            @Override
            public void onResponse(Call<List<TourPackageDTO>> call, Response<List<TourPackageDTO>> response) {
                Log.d("API_PROVERA", "Šaljem zahtev za User: " + userId + " i Kategoriju: " + catId);
                Log.d("API", "Kod: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    List<TourPackageDTO> list = response.body();
                    Log.d("API_PROVERA", "Stiglo aranžmana: " + list.size());

                    if (adapter == null) {
                        adapter = new DestinationAdapter(list);
                        rvFavorites.setAdapter(adapter);
                    } else {
                        adapter.updateList(list);
                    }

                    // Pokaži poruku ako je lista stvarno prazna
                    findViewById(R.id.layoutEmpty).setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<TourPackageDTO>> call, Throwable t) {
                Log.e("API_ERROR", "Greška: " + t.getMessage());
            }
        });
    }
}