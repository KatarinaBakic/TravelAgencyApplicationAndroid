package com.example.travelagencyapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.travelagencyapplication.api.ApiService;
import com.example.travelagencyapplication.api.RetrofitClient;
import com.example.travelagencyapplication.model.TourPackageDTO;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DestinationDetailActivity extends AppCompatActivity {

    private TextView tvNaslov, tvOpis, tvCena, tvOcena;
    private ImageView ivDetaljiSlika;
    private RatingBar rbDetaljiOcena;
    private EditText etNoviKomentar;
    private Button btnPosaljiKomentar, btnVidiSveRecenzije;
    private RatingBar rbUnosOcene;
    private LinearLayout layoutUnosKomentara;
    private Double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_destination_detail);

        tvNaslov            = findViewById(R.id.tvDetaljiNaslov);
        tvOpis              = findViewById(R.id.tvDetaljiOpis);
        tvCena              = findViewById(R.id.tvDetaljiCena);
        ivDetaljiSlika      = findViewById(R.id.ivDetaljiSlika);
        rbDetaljiOcena      = findViewById(R.id.rbDetaljiOcena);
        rbUnosOcene         = findViewById(R.id.rbUnosOcene);
        etNoviKomentar      = findViewById(R.id.etNoviKomentar);
        btnPosaljiKomentar  = findViewById(R.id.btnPosaljiKomentar);
        btnVidiSveRecenzije = findViewById(R.id.btnVidiSveRecenzije);
        layoutUnosKomentara = findViewById(R.id.layoutUnosKomentara);

        if (isUserLoggedIn()) {
            layoutUnosKomentara.setVisibility(View.VISIBLE);
        } else {
            layoutUnosKomentara.setVisibility(View.GONE);
            Toast.makeText(this, "Ulogujte se da biste ostavili recenziju", Toast.LENGTH_SHORT).show();
        }

        long packageTourId = getIntent().getLongExtra("packageTourId", -1L);
        Log.e("packageTourId", String.valueOf(packageTourId));
        if (packageTourId != -1) {
            ucitajDetalje(packageTourId);
        }

        btnPosaljiKomentar.setOnClickListener(v -> {
            posaljiRecenziju(packageTourId);
        });

        btnVidiSveRecenzije.setOnClickListener(v -> {
            Intent intent = new Intent(DestinationDetailActivity.this, ReviewsActivity.class);
            intent.putExtra("packageTourId", packageTourId); // packageTourId je onaj koji već imaš u aktivnosti
            startActivity(intent);
        });

        FloatingActionButton fabMap = findViewById(R.id.fabMap);
        fabMap.setOnClickListener(v -> showOnMap());

        // Rešavanje padding-a za ekrane sa urezima (notch)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.collapsing_toolbar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void ucitajDetalje(long id) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<TourPackageDTO> call = apiService.getDetails((int) id);

        call.enqueue(new Callback<TourPackageDTO>() {
            @Override
            public void onResponse(Call<TourPackageDTO> call, Response<TourPackageDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TourPackageDTO dto = response.body();

                    tvNaslov.setText(dto.getTitle());
                    tvOpis.setText(dto.getDescription());
                    tvCena.setText("Cena: " + dto.getPrice() + " EUR");

                    if (dto.getAverageRating() != null) {
                        rbDetaljiOcena.setRating(dto.getAverageRating().floatValue());
                    }

                    latitude  = dto.getLatitude();
                    longitude = dto.getLongitude();

                    String slikaUrl = RetrofitClient.SERVER_URL + dto.getImageUrl();
                    Log.e("slikaUrl", slikaUrl);
                    com.bumptech.glide.Glide.with(DestinationDetailActivity.this)
                            .load(slikaUrl)
                            .placeholder(R.drawable.default_url_1)
                            .into(ivDetaljiSlika);
                }
            }

            @Override
            public void onFailure(Call<TourPackageDTO> call, Throwable t) {
                Toast.makeText(DestinationDetailActivity.this, "Greška pri učitavanju", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void posaljiRecenziju(long packageId) {
        SharedPreferences sp = getSharedPreferences("TravelAgencyApplication", MODE_PRIVATE);
        String userIdStr     = sp.getString("userId", "-1");

        if (userIdStr.equals("-1")) {
            Toast.makeText(this, "Niste ulogovani!", Toast.LENGTH_SHORT).show();
            return;
        }
        long ulogovanKorisnikId = Long.parseLong(userIdStr);
        String tekst            = etNoviKomentar.getText().toString();
        int ocena               = (int) rbUnosOcene.getRating();
        if (ocena == 0) {
            Toast.makeText(this, "Molimo Vas izaberite ocenu!", Toast.LENGTH_SHORT).show();
            return;
        }

        com.example.travelagencyapplication.model.ReviewDTO reviewDto =
                new com.example.travelagencyapplication.model.ReviewDTO(tekst, ocena, ulogovanKorisnikId, packageId);

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        apiService.postReview(reviewDto).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DestinationDetailActivity.this, "Hvala na recenziji!", Toast.LENGTH_SHORT).show();
                    etNoviKomentar.setText("");
                    rbUnosOcene.setRating(0);
                } else {
                    Toast.makeText(DestinationDetailActivity.this, "Greška pri slanju", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                Toast.makeText(DestinationDetailActivity.this, "Problem sa mrežom", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showOnMap() {
        if (latitude != null && longitude != null) {
            String label = tvNaslov.getText().toString();
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + latitude + "," + longitude + "(" + Uri.encode(label) + ")");

            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // Ako korisnik nema instalirane Google Mape, otvori u browseru
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/maps/search/?api=1&query=" + latitude + "," + longitude));
                startActivity(browserIntent);
            }
        } else {
            Toast.makeText(this, "Lokacija za ovaj aranžman nije dostupna", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isUserLoggedIn() {
        SharedPreferences sp = getSharedPreferences("TravelAgencyApplication", MODE_PRIVATE);
        String userIdStr = sp.getString("userId", "-1");
        return userIdStr != null && !userIdStr.equals("-1");
    }
}