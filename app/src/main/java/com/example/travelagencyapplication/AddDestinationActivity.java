package com.example.travelagencyapplication;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.travelagencyapplication.api.ApiService;
import com.example.travelagencyapplication.api.RetrofitClient;
import com.example.travelagencyapplication.model.TourPackageCategory;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddDestinationActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etDescription, etPrice;
    private Spinner spinnerCategory;
    private ImageView ivPreview;
    private Uri selectedImageUri;
    private ApiService apiService;
    private long currentAdminId;
    private List<TourPackageCategory> categories = new ArrayList<>();
    private GoogleMap mMap;
    private LatLng selectedLatLng;
    private ImageButton btnMapSearch;
    private Button btnSaveDestination, btnDestSelectImage;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    ivPreview.setImageURI(selectedImageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_destination);

        etTitle            = findViewById(R.id.etDestTitle);
        etDescription      = findViewById(R.id.etDestDescription);
        etPrice            = findViewById(R.id.etDestPrice);
        spinnerCategory    = findViewById(R.id.spinnerDestCategory);
        ivPreview          = findViewById(R.id.ivDestPreview);
        apiService         = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        currentAdminId     = getIntent().getLongExtra("userId", -1);
        btnMapSearch       = findViewById(R.id.btnMapSearch);;
        btnSaveDestination = findViewById(R.id.btnSaveDestination);
        btnDestSelectImage = findViewById(R.id.btnDestSelectImage);

        btnMapSearch.setOnClickListener(v -> searchLocation());
        loadCategories();

        setupMap();
        btnDestSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        btnSaveDestination.setOnClickListener(v ->{
            Log.d("CLICK_CHECK", "Dugme je kliknuto!");
            saveDestination();
        });
    }

    private void loadCategories() {
        apiService.getAllCategories().enqueue(new Callback<List<TourPackageCategory>>() {
            @Override
            public void onResponse(Call<List<TourPackageCategory>> call, Response<List<TourPackageCategory>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body();

                    ArrayAdapter<TourPackageCategory> adapter = new ArrayAdapter<>(
                            AddDestinationActivity.this,
                            android.R.layout.simple_spinner_item,
                            categories
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategory.setAdapter(adapter);

                    long currentCatId = getIntent().getLongExtra("categoryId", -1L);
                    if (currentCatId != -1L) {
                        for (int i = 0; i < categories.size(); i++) {
                            if (categories.get(i).getId().longValue() == currentCatId) {
                                spinnerCategory.setSelection(i);
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<TourPackageCategory>> call, Throwable t) {
                Log.e("API_ERROR", "Greška pri učitavanju kategorija");
            }
        });
    }

    private void saveDestination() {
        Log.e("SAVE DESTINATION", "SAVE");
        String title       = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String price       = etPrice.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || price.isEmpty() || selectedImageUri == null) {
            Toast.makeText(this, "Molimo popunite sva polja i izaberite sliku", Toast.LENGTH_SHORT).show();
            return;
        }

        TourPackageCategory selectedCategory = (TourPackageCategory) spinnerCategory.getSelectedItem();
        if (selectedCategory == null) {
            Toast.makeText(this, "Izaberite kategoriju", Toast.LENGTH_SHORT).show();
            return;
        }
        String categoryId = String.valueOf(selectedCategory.getId());

        if (selectedLatLng == null) {
            Toast.makeText(this, "Molimo izaberite lokaciju na mapi!", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody rbTitle          = RequestBody.create(MultipartBody.FORM, title);
        RequestBody rbDescription    = RequestBody.create(MultipartBody.FORM, description);
        RequestBody rbPrice          = RequestBody.create(MultipartBody.FORM, price);
        RequestBody rbCategoryId     = RequestBody.create(MultipartBody.FORM, categoryId);
        RequestBody rbUserId         = RequestBody.create(MultipartBody.FORM, String.valueOf(currentAdminId));
        RequestBody rbLat            = RequestBody.create(MultipartBody.FORM, String.valueOf(selectedLatLng.latitude));
        RequestBody rbLon            = RequestBody.create(MultipartBody.FORM, String.valueOf(selectedLatLng.longitude));
        MultipartBody.Part imagePart = prepareFilePart("image", selectedImageUri);
        Log.e("SAVE DESTINATION_LAB I LON", rbLat + ", " + rbLon);
        apiService.addDestination(rbTitle, rbDescription, rbPrice, rbCategoryId, rbUserId, rbLat, rbLon, imagePart)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(AddDestinationActivity.this, "Destinacija uspešno objavljena!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            try {
                                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                                Log.e("API_ERROR", "Kod: " + response.code() + " Poruka: " + errorBody);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }}
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("API_ERROR", "Greška: " + t.getMessage());
                        Toast.makeText(AddDestinationActivity.this, "Greška u komunikaciji sa serverom", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            byte[] bytes = byteBuffer.toByteArray();

            RequestBody requestFile = RequestBody.create(
                    MediaType.parse(getContentResolver().getType(fileUri)),
                    bytes
            );

            return MultipartBody.Part.createFormData(partName, "destination_image.jpg", requestFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);

        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                mMap = googleMap;

                // Početno fokusiranje na neku lokaciju (npr. tvoj grad)
                LatLng defaultPos = new LatLng(44.7866, 20.4489);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultPos, 10f));

                // Slušalac za klik
                mMap.setOnMapClickListener(latLng -> {
                    mMap.clear(); // Briše stari marker
                    mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("Lokacija destinacije"));

                    selectedLatLng = latLng; // Čuvamo vrednost za saveDestination()
                    Log.d("MAP_CLICK", "Izabrano: " + latLng.latitude + ", " + latLng.longitude);
                });
            });
        }
    }

    private void searchLocation() {
        EditText etMapSearch = findViewById(R.id.etMapSearch);
        String location = etMapSearch.getText().toString();

        if (location.isEmpty()) return;

        android.location.Geocoder geocoder = new android.location.Geocoder(this);
        try {
            List<android.location.Address> addressList = geocoder.getFromLocationName(location, 1);
            if (addressList != null && !addressList.isEmpty()) {
                android.location.Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f));

                // Sačuvaj lokaciju za bazu
                selectedLatLng = latLng;
            } else {
                Toast.makeText(this, "Lokacija nije pronađena", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}