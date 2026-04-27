package com.example.travelagencyapplication;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
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
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCategoryActivity extends AppCompatActivity {

    private TextInputEditText etName, etDescription;
    private ImageView ivPreview;
    private Uri selectedImageUri;
    private ApiService apiService;
    Button btnSelectImage, btnSave;
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
        setContentView(R.layout.activity_add_category);

        etName         = findViewById(R.id.etCategoryName);
        etDescription  = findViewById(R.id.etCategoryDescription);
        ivPreview      = findViewById(R.id.ivCategoryPreview);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSave        = findViewById(R.id.btnSaveCategory);

        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });
        btnSave.setOnClickListener(v -> uploadCategory());
    }
    private void uploadCategory() {
        String name = etName.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        if (name.isEmpty() || selectedImageUri == null) {
            Toast.makeText(this, "Ime i slika su obavezni!", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody rbName      = RequestBody.create(MultipartBody.FORM, name);
        RequestBody rbDesc      = RequestBody.create(MultipartBody.FORM, desc);
        MultipartBody.Part body = prepareFilePart("imageCat", selectedImageUri);

        apiService.addCategory(rbName, rbDesc, body).enqueue(new Callback<TourPackageCategory>() {
            @Override
            public void onResponse(Call<TourPackageCategory> call, Response<TourPackageCategory> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddCategoryActivity.this, "Kategorija uspešno dodata!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddCategoryActivity.this, "Greška na serveru: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<TourPackageCategory> call, Throwable t) {
                Log.e("API_UPLOAD", "Greška: " + t.getMessage());
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

            return MultipartBody.Part.createFormData(partName, "image.jpg", requestFile);

        } catch (IOException e) {
            Log.e("UPLOAD_ERROR", "Greška pri čitanju slike: " + e.getMessage());
            return null;
        }
    }
}