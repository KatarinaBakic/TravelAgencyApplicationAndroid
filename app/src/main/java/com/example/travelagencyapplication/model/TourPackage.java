package com.example.travelagencyapplication.model;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TourPackage {
    @SerializedName("id")
    private int id;
    private String title;
    private String description;
    private double price;

    private List<TourPackageImage> tourpackageimages;

    // Pomoćna metoda da izvučeš samo glavnu sliku
    public String getMainImageUrl() {
        if (tourpackageimages != null && !tourpackageimages.isEmpty()) {
            android.util.Log.d("DEBUG_ADAPTER", "Lista slika nije prazna, broj slika: " + tourpackageimages.size());
            for (TourPackageImage img : tourpackageimages) {
                // Ako je slika označena kao glavna u bazi (isMain = 1)
                android.util.Log.e("DEBUG_SLIKA", img.getImageUrl());
                if (img.getIsMain() == 1) {
                    return img.getImageUrl();
                };
            }
            // Ako nema glavne, vrati prvu bilo koju da ne bude prazno
            return tourpackageimages.get(0).getImageUrl();
        }
        android.util.Log.e("DEBUG_ADAPTER", "LISTA SLIKA JE NULL ILI PRAZNA!");
        return null; // Vraća null samo ako je lista potpuno prazna
    }

}
