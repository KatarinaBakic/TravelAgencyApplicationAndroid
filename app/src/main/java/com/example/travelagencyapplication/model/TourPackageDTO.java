package com.example.travelagencyapplication.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TourPackageDTO {
    private Integer id;
    private String title;
    private String description;
    private Float price;
    private Double averageRating; // Za ocene iz Packagereview
    @SerializedName("tourpackageimages")
    private List<TourPackageImage> tourpackageimages;

    // Prazan konstruktor (neophodan za Retrofit/Gson)
    public TourPackageDTO() {}

    // Getteri i Setteri
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Float getPrice() { return price; }
    public void setPrice(Float price) { this.price = price; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public String getImageUrl() {
        if (tourpackageimages != null && !tourpackageimages.isEmpty()) {
            // Tražimo sliku koja ima isMain == 1
            for (TourPackageImage img : tourpackageimages) {
                if (img.getIsMain() == 1) {
                    return img.getImageUrl();
                }
            }
            // Ako nema glavne, uzmi bilo koju prvu iz liste
            return tourpackageimages.get(0).getImageUrl();
        }
        return null;
    }
    public List<TourPackageImage> getTourpackageimages() { return tourpackageimages; }
    public void setTourpackageimages(List<TourPackageImage> tourpackageimages) {
        this.tourpackageimages = tourpackageimages;
    }
}
