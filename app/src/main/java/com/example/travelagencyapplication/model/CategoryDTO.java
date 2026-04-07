package com.example.travelagencyapplication.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CategoryDTO {
    private String name;
    @SerializedName("tourPackages")
    private List<TourPackageDTO> tourpackages;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TourPackageDTO> getTourpackages() {
        return tourpackages;
    }

    public void setTourpackages(List<TourPackageDTO> tourpackages) {
        this.tourpackages = tourpackages;
    }
}
