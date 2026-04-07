package com.example.travelagencyapplication.utils;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.travelagencyapplication.api.ApiService;
import com.example.travelagencyapplication.model.TourPackageCategory;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryUtils {

    // Definišemo interfejs kako bi Activity znao kada je korisnik izabrao kategoriju
    public interface CategorySelectionListener {
        void onCategorySelected(Long categoryId);
    }

    public static void setupCategorySpinner(Context context, ApiService apiService, Spinner spinner, CategorySelectionListener listener) {
        apiService.getAllCategories().enqueue(new Callback<List<TourPackageCategory>>() {
            @Override
            public void onResponse(Call<List<TourPackageCategory>> call, Response<List<TourPackageCategory>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TourPackageCategory> categories = response.body();
                    List<String> names = new ArrayList<>();
                    names.add("Sve kategorije");

                    for (TourPackageCategory c : categories) {
                        names.add(c.getName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                            android.R.layout.simple_spinner_item, names);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            Long selectedId = (position == 0) ? -1L : (long) categories.get(position - 1).getId();
                            listener.onCategorySelected(selectedId);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<TourPackageCategory>> call, Throwable t) {
                Log.e("API_ERROR", "Greška pri učitavanju kategorija: " + t.getMessage());
            }
        });
    }
}