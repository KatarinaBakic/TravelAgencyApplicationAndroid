package com.example.travelagencyapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelagencyapplication.adapters.DestinationAdapter;
import com.example.travelagencyapplication.api.ApiService;
import com.example.travelagencyapplication.api.RetrofitClient;
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

        SharedPreferences sp = getSharedPreferences("TravelAgencyApplication", MODE_PRIVATE);
        String userIdStr = sp.getString("userId", "-1");

        Toolbar toolbar = findViewById(R.id.toolbarFavorites);
        setSupportActionBar(toolbar);

        if (!userIdStr.equals("-1")) {
            loadFavorites(Long.parseLong(userIdStr));

            Spinner spinner = findViewById(R.id.spinnerFavCategories);
            CategoryUtils.setupCategorySpinner(this, apiService, spinner, categoryId -> {
                loadFavorites(categoryId);
            });
        }
    }
    private void loadFavorites(Long catId) {
        SharedPreferences sp = getSharedPreferences("TravelAgencyApplication", MODE_PRIVATE);
        long userId          = Long.parseLong(sp.getString("userId", "-1"));
        String userRole = sp.getString("userRole", "ROLE_USER");

        apiService.getFavoritePackages(userId, catId).enqueue(new Callback<List<TourPackageDTO>>() {
            @Override
            public void onResponse(Call<List<TourPackageDTO>> call, Response<List<TourPackageDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TourPackageDTO> list = response.body();
                    if (adapter == null) {
                        adapter = new DestinationAdapter(list, userRole);
                        rvFavorites.setAdapter(adapter);
                    } else {
                        adapter.updateList(list);
                    }
                    findViewById(R.id.layoutEmpty).setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<TourPackageDTO>> call, Throwable t) {
                Log.e("API_ERROR", "Greška: " + t.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    //TODO: dopuniti akcije i proveriti celokupan meni
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            return true;
        } else if (id == R.id.nav_logout) {

            SharedPreferences sp = getSharedPreferences("TravelAgencyApplication", MODE_PRIVATE);
            sp.edit().remove("userId").apply();

            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        } else if(id == R.id.nav_profile){
            Intent intent = new Intent(FavoritesActivity.this, UserPageActivity.class);
            startActivity(intent);
            return true;
        }else if (id == R.id.nav_friends) {
            Intent intent = new Intent(FavoritesActivity.this, FriendsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}