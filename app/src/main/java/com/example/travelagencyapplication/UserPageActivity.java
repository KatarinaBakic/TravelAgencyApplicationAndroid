package com.example.travelagencyapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelagencyapplication.adapters.DestinationAdapter;
import com.example.travelagencyapplication.api.ApiService;
import com.example.travelagencyapplication.api.RetrofitClient;
import com.example.travelagencyapplication.model.TourPackageCategory;
import com.example.travelagencyapplication.model.TourPackageDTO;
import com.example.travelagencyapplication.model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.view.Menu;


public class UserPageActivity extends AppCompatActivity {

    ApiService apiService;
    TextView tvWelcome, tvEmail, tvLastname, tvFirstname;
    RecyclerView rvDestinations;
    DestinationAdapter adapter;
    List<TourPackageCategory> categories= new ArrayList<>();
    User loggedInUser;
    Button btnDeletedDestinations, fabAddDestination, fabAddCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_page);

        Toolbar toolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);

        tvWelcome              = findViewById(R.id.tvWelcome);
        tvEmail                = findViewById(R.id.tvEmail);
        tvFirstname            = findViewById(R.id.tvFirstname);
        tvLastname             = findViewById(R.id.tvLastname);
        rvDestinations         = findViewById(R.id.rvDestinations);
        btnDeletedDestinations = findViewById(R.id.btnDeletedDestinations);
        fabAddDestination      = findViewById(R.id.fabAddDestination);
        fabAddCategory         = findViewById(R.id.fabAddCategory);

        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        rvDestinations.setLayoutManager(new GridLayoutManager(this, 2));

        SharedPreferences sp = getSharedPreferences("TravelAgencyApplication", MODE_PRIVATE);
        String currentUser   = sp.getString("userId", "-1"); // "-1" je default ako ne nađe ništa

        if (!currentUser.equals("-1")) {
            loadUserInfo(currentUser);

            Button btnEditProfile = findViewById(R.id.btnEditProfile);
            btnEditProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEditProfileDialog(loggedInUser);
                }
            });

            btnDeletedDestinations.setOnClickListener(v -> {
                Intent intent = new Intent(UserPageActivity.this, DeletedDestinationsActivity.class);
                startActivity(intent);
            });

            fabAddDestination.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddDestinationActivity.class);
                intent.putExtra("userId", loggedInUser.getId());
                startActivity(intent);
            });

            fabAddCategory.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddCategoryActivity.class);
                startActivity(intent);
            });

            loadCategories();
        } else {
            tvWelcome.setText("Dobrodošli, gost!");
            tvEmail.setText("Prijavite se za više opcija");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPackagesFromServer();
        loadCategories();
    }
    private void loadPackagesFromServer() {
        apiService.getTourPackages("active", -1L).enqueue(new Callback<List<TourPackageDTO>>() {
            @Override
            public void onResponse(Call<List<TourPackageDTO>> call, Response<List<TourPackageDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TourPackageDTO> packages = response.body();
                    String role = (loggedInUser != null) ? loggedInUser.getRole() : "ROLE_USER";

                    adapter = new DestinationAdapter(packages, role);
                    rvDestinations.setAdapter(adapter);
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
        }  else if (id == R.id.nav_favorites) {
            Intent intent = new Intent(UserPageActivity.this, FavoritesActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_friends) {
            Intent intent = new Intent(UserPageActivity.this, FriendsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadUserInfo(String userId) {
        long id = Long.parseLong(userId);

        apiService.getUserByUserId(id).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    loggedInUser = response.body();
                    loadPackagesFromServer();

                    tvWelcome.setText("Dobrodošli, " + loggedInUser.getUsername() + "!");
                    tvEmail.setText("Email: " + loggedInUser.getEmail());
                    tvFirstname.setText("Firstname: " + loggedInUser.getFirstName());
                    tvLastname.setText("Lastname: " + loggedInUser.getLastName());

                    if (loggedInUser.getRole() != null && loggedInUser.getRole().equals("ROLE_ADMIN")) {
                        btnDeletedDestinations.setVisibility(View.VISIBLE);
                        fabAddDestination.setVisibility(View.VISIBLE);
                        fabAddCategory.setVisibility(View.VISIBLE);
                    } else {
                        btnDeletedDestinations.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("API_ERROR", "Ne mogu da učitam podatke korisnika: " + t.getMessage());
            }
        });
    }
    public void loadCategories() {
        apiService.getAllCategories().enqueue(new Callback<List<TourPackageCategory>>() {
            @Override
            public void onResponse(Call<List<TourPackageCategory>> call, Response<List<TourPackageCategory>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body();

                    List<String> categoriesNames = new ArrayList<>();
                    categoriesNames.add("Sve kategorije");
                    for (TourPackageCategory c : categories) {
                        categoriesNames.add(c.getName());
                    }

                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                            UserPageActivity.this,
                            android.R.layout.simple_spinner_item,
                            categoriesNames
                    );
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    Spinner spinner = findViewById(R.id.spinnerCategories);
                    spinner.setAdapter(spinnerAdapter);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { // OVDE SAMO AdapterView.
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (position == 0) {
                                filterPackages((long) -1);
                            } else {
                                Long categoryId = (long) categories.get(position - 1).getId();
                                filterPackages(categoryId);
                            }
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            // Može ostati prazno
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
    private void filterPackages(Long categoryId) {
        apiService.getTourPackages("active", categoryId).enqueue(new Callback<List<TourPackageDTO>>() {
            @Override
            public void onResponse(Call<List<TourPackageDTO>> call, Response<List<TourPackageDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TourPackageDTO> filterDestinations = response.body();
                    if (adapter != null) {
                        adapter.updateList(filterDestinations);
                    }
                }
            }
            @Override
            public void onFailure(Call<List<TourPackageDTO>> call, Throwable t) {
                Log.e("FILTER_ERROR", "Greška pri filtriranju: " + t.getMessage());
            }
        });
    }

    //TODO: SREDITI ODJAVU KADA SE IZADJE IZ APLIKACIJE BEZ ODJAVE PREKO MENIJA!
    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences sp = getSharedPreferences("TravelAgencyApplication", MODE_PRIVATE);
        sp.edit().remove("userId").apply();
    }

    private void showEditProfileDialog(User currentUser) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.user_page_edit_profile, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // Za zaobljene ivice

        EditText etFirstName       = dialogView.findViewById(R.id.etEditFirstName);
        EditText etLastName        = dialogView.findViewById(R.id.etEditLastName);
        EditText etEmail           = dialogView.findViewById(R.id.etEditEmail);
        EditText etEditPassword    = dialogView.findViewById(R.id.etEditPassword);
        EditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);
        TextView tvError           = dialogView.findViewById(R.id.tvErrorMessage);
        Button btnSave             = dialogView.findViewById(R.id.btnSaveEdit);
        Button btnCancel           = dialogView.findViewById(R.id.btnCancelEdit);

        if (currentUser != null) {
            etFirstName.setText(currentUser.getFirstName());
            etLastName.setText(currentUser.getLastName());
            etEmail.setText(currentUser.getEmail());
        }
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String newFirstName       = etFirstName.getText().toString().trim();
            String newLastName        = etLastName.getText().toString().trim();
            String newEmail           = etEmail.getText().toString().trim();
            String newPassword        = etEditPassword.getText().toString().trim(); // Dodaj ove EditText-ove u XML
            String newConfirmPassword = etConfirmPassword.getText().toString().trim();
            tvError.setVisibility(View.GONE);

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                tvError.setText("Unesite ispravnu email adresu \n(npr. ime@gmail.com)!");
                tvError.setVisibility(View.VISIBLE);
            } else {
                apiService.updateProfile(currentUser.getId(), currentUser, newPassword, newConfirmPassword).enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {

                        if (response.isSuccessful()) {
                            tvFirstname.setText("Firstname: " + newFirstName);
                            tvLastname.setText("Lastname: " + newLastName);
                            tvEmail.setText("Email: " + newEmail);
                            currentUser.setFirstName(newFirstName);
                            currentUser.setLastName(newLastName);
                            currentUser.setEmail(newEmail);

                            Toast.makeText(UserPageActivity.this, "Profil je uspesno ažuriran!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            try {
                                String errorMsg = response.errorBody().string();

                                tvError.setText(errorMsg);
                                tvError.setVisibility(View.VISIBLE);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Log.e("UPDATE_USER_ERROR", "Greška pri čuvanju novih podataka za korisnika: " + t.getMessage());
                    }
                });
            }
        });
        dialog.show();
    }
}