package com.example.travelagencyapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.travelagencyapplication.api.ApiService;
import com.example.travelagencyapplication.api.RetrofitClient;
import com.example.travelagencyapplication.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainActivity extends AppCompatActivity {

    ApiService apiService;
    Button btnLogin;
    EditText etEmail,etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.btnLogin), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        this.etEmail    = findViewById(R.id.etUsername);
        this.etPassword = findViewById(R.id.etPassword);
        this.btnLogin   = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Popunite sva polja", Toast.LENGTH_SHORT).show();
                } else {
                    // OVDE pozivamo tvoju metodu koja komunicira sa API-jem
                    loginUser(username, password);
                }
            }
        });

        findViewById(R.id.btnGoToDestinations).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AvailableDestinationsActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser(String username, String password) {
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        // 2. Kreiranje objekta koji šaljemo
        User loginPodaci = new User(username, password);
        Log.e("loginPodaci_2", username + "  " + password);
        // 3. Izvršavanje poziva
        apiService.login(loginPodaci).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    String userId = response.body();

                    // Otvaramo "skladište"
                    SharedPreferences sp = getSharedPreferences("TravelAgencyApplication", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();

                    //TODO: razmisliti da li da se sve informacije cuvaju ovde, pa da se salju (da ne bude jos jedan poziv ka servisu) ili samo userId
                    editor.putString("userId", userId);
                    editor.putBoolean("isLoggedIn", true);
                    editor.apply(); // Snimi promene

                    Toast.makeText(MainActivity.this, "Uspešan login! Token: " + userId, Toast.LENGTH_LONG).show();
                    // Ovde možeš prebaciti korisnika na drugi ekran

                    Intent intent = new Intent(MainActivity.this, UserPageActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Greška: Proverite podatke", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Greška u mrežnoj komunikaciji: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}