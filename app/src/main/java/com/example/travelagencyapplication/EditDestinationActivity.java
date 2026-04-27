package com.example.travelagencyapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
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
import com.bumptech.glide.Glide;
import com.example.travelagencyapplication.api.ApiService;
import com.example.travelagencyapplication.api.RetrofitClient;
import com.example.travelagencyapplication.model.TourPackageCategory;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditDestinationActivity extends AppCompatActivity {
    EditText etTitle, etDescription, etPrice;
    Spinner spinnerCategory;
    Button btnSave, btnSelectImage;
    Long destinationId;
    ApiService apiService;
    private Uri selectedImageUri;
    ImageView ivDestination;
    private GoogleMap mMap;
    private LatLng selectedLatLng;
    private Double existingLat, existingLon;
    private ImageButton btnMapSearch;
    List<TourPackageCategory> categories = new ArrayList<>();

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    Glide.with(this).load(selectedImageUri).into(ivDestination);
                    Toast.makeText(this, "Nova slika spremna za čuvanje!", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_destination);
        try {
            etTitle         = findViewById(R.id.etEditTitle);
            etDescription   = findViewById(R.id.etEditDescription);
            etPrice         = findViewById(R.id.etEditPrice);
            spinnerCategory = findViewById(R.id.spinnerEditCategory);
            ivDestination   = findViewById(R.id.ivEditDestination);
            btnSelectImage  = findViewById(R.id.btnSelectNewImage);
            btnSave         = findViewById(R.id.btnSaveDestination);
            btnMapSearch    = findViewById(R.id.btnEditMapSearch);;
            apiService      = RetrofitClient.getRetrofitInstance().create(ApiService.class);

            destinationId = getIntent().getLongExtra("destinationId", -1L);
            existingLat   = getIntent().getDoubleExtra("latitude", 0.0);
            existingLon   = getIntent().getDoubleExtra("longitude", 0.0);

            loadCategories();
            fillFieldFromIntent();

            if (existingLat != 0.0 && existingLon != 0.0) {
                selectedLatLng = new LatLng(existingLat, existingLon);
            }

            btnMapSearch.setOnClickListener(v -> searchLocation());
            setupMap();

            btnSelectImage.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                imagePickerLauncher.launch(intent);
            });

            btnSave.setOnClickListener(v -> updateData());
        } catch (Exception e) {
            Toast.makeText(this, "Greška pri otvaranju forme", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void fillFieldFromIntent() {
        etTitle.setText(getIntent().getStringExtra("title"));
        etDescription.setText(getIntent().getStringExtra("desc"));
        etPrice.setText(String.valueOf(getIntent().getDoubleExtra("price", 0)));
        //spinnerCategory.setSelection(getIntent().getStringExtra("categoryId"));

        long catId  = getIntent().getLongExtra("categoryId", -1L);
        String path = getIntent().getStringExtra("imageUrl"); // Moraš proslediti imageUrl iz adaptera!
        if (path != null) {
            Glide.with(this)
                    .load(RetrofitClient.BASE_URL + path)
                    .into(ivDestination);
        }
    }
    private void updateData() {
        if (categories == null || categories.isEmpty()) {
            Toast.makeText(this, "Kategorije još nisu učitane", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedPosition  = spinnerCategory.getSelectedItemPosition();
        Long selectedCatId    = categories.get(selectedPosition).getId();
        RequestBody idBody    = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(destinationId));
        RequestBody titleBody = RequestBody.create(MediaType.parse("text/plain"), etTitle.getText().toString());
        RequestBody descBody  = RequestBody.create(MediaType.parse("text/plain"), etDescription.getText().toString());
        RequestBody priceBody = RequestBody.create(MediaType.parse("text/plain"), etPrice.getText().toString());
        RequestBody catIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(selectedCatId)); // Primer za kategoriju
        RequestBody rbLat     = RequestBody.create(MultipartBody.FORM, String.valueOf(selectedLatLng.latitude));
        RequestBody rbLon     = RequestBody.create(MultipartBody.FORM, String.valueOf(selectedLatLng.longitude));

        MultipartBody.Part imagePart = null;
        if (selectedImageUri != null) {
            imagePart = prepareImagePart(selectedImageUri);
        }

        apiService.updateTourPackage(idBody, titleBody, descBody, priceBody, catIdBody, rbLat, rbLon, imagePart)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(EditDestinationActivity.this, "Uspešno izmenjeno!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(EditDestinationActivity.this, "Server vratio grešku: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Log.e("EDIT_ERROR", t.getMessage());
                    }
                });
    }

    private MultipartBody.Part prepareImagePart(Uri fileUri) {
        try {
            Bitmap bitmap        = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fileUri);
            File tempFile        = new File(getCacheDir(), "temp_image_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos);
            fos.flush();
            fos.close();

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), tempFile);
            return MultipartBody.Part.createFormData("imageFile", tempFile.getName(), requestFile);
        } catch (Exception e) {
            Log.e("UPLOAD_ERROR", "Greška pri kompresiji: " + e.getMessage());
            return null;
        }
    }

    private void loadCategories() {
        apiService.getAllCategories().enqueue(new Callback<List<TourPackageCategory>>() {
            @Override
            public void onResponse(Call<List<TourPackageCategory>> call, Response<List<TourPackageCategory>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body();
                    List<String> categoryNames = new ArrayList<>();
                    for (TourPackageCategory c : categories) {
                        categoryNames.add(c.getName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            EditDestinationActivity.this,
                            android.R.layout.simple_spinner_item,
                            categoryNames
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
    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.editMapFragment);

        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                mMap = googleMap;

                // Ako imamo stare koordinate, postavi marker tamo
                if (selectedLatLng != null) {
                    mMap.addMarker(new MarkerOptions().position(selectedLatLng).title("Trenutna lokacija"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 12f));
                }

                // Dozvoli promenu lokacije klikom
                mMap.setOnMapClickListener(latLng -> {
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(latLng).title("Nova lokacija"));
                    selectedLatLng = latLng;
                });
            });
        }
    }
    private void searchLocation() {
        EditText etMapSearch = findViewById(R.id.etEditMapSearch);
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

