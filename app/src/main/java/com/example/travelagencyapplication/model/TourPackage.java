package com.example.travelagencyapplication.model;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.util.List;

//TODO: OBRISATI KLASU -> KORISTI SE DTO
public class TourPackage {
    @SerializedName("id")
    private int id;
    private String title;
    private String description;
    private double price;
    private List<TourPackageImage> tourpackageimages;

    public String getMainImageUrl() {
        if (tourpackageimages != null && !tourpackageimages.isEmpty()) {
            for (TourPackageImage img : tourpackageimages) {
                if (img.getIsMain() == 1) {
                    return img.getImageUrl();
                };
            }
            return tourpackageimages.get(0).getImageUrl();
        }
        return null;
    }
}
