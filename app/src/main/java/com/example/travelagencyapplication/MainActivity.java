package com.example.travelagencyapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.travelagencyapplication.api.ApiService;
import com.example.travelagencyapplication.api.RetrofitClient;
import com.example.travelagencyapplication.model.User;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainActivity extends AppCompatActivity {

    ApiService apiService;
    Button btnLogin, btnGoToDestinations;
    EditText etEmail,etPassword;
    TextView tvForgotPassword;

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

        etEmail             = findViewById(R.id.etUsername);
        etPassword          = findViewById(R.id.etPassword);
        btnLogin            = findViewById(R.id.btnLogin);
        btnGoToDestinations = findViewById(R.id.btnGoToDestinations);
        tvForgotPassword    = findViewById(R.id.tvForgotPassword);

        tvForgotPassword.setOnClickListener(v -> {
            showForgotPasswordDialog();
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Popunite sva polja", Toast.LENGTH_SHORT).show();
                } else {
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
        User loginPodaci = new User(username, password);

        apiService.login(loginPodaci).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    String userId = response.body();
                    SharedPreferences sp            = getSharedPreferences("TravelAgencyApplication", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();

                    editor.putString("userId", userId);
                    editor.putBoolean("isLoggedIn", true);
                    editor.putString("userRole", loginPodaci.getRole());
                    editor.apply();

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

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Zaboravljena lozinka");
        builder.setMessage("Unesite email na koji ćemo poslati link za reset:");

        // Kreiramo polje za unos (EditText) dinamički
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setHint("Vaš email");
        builder.setView(input);

        builder.setPositiveButton("Pošalji", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (!email.isEmpty()) {
                sendResetLink(email);
            } else {
                Toast.makeText(this, "Morate uneti email!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Otkaži", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void sendResetLink(String email) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        // Ovde menjamo Void u Map<String, String>
        apiService.forgotPassword(email).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Proverite email za dalja uputstva!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Email nije pronađen ili greška na serveru.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Greška u komunikaciji.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}