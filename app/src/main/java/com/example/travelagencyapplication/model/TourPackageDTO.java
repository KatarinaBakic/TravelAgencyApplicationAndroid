package com.example.travelagencyapplication.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TourPackageDTO {
    private Long id;
    private String title;
    private String description;
    private Float price;
    private Double averageRating;
    private Integer isDeleted;
    private Long categoryId;
    private Double latitude;
    private Double longitude;
    @SerializedName("tourpackageimages")
    private List<TourPackageImage> tourpackageimages;

    public TourPackageDTO() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Float getPrice() { return price; }
    public void setPrice(Float price) { this.price = price; }
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }

    //uzimamo glavnu sliku -> ukoliko je nema uzimamo bilo koju prvu iz liste
    public String getImageUrl() {
        if (tourpackageimages != null && !tourpackageimages.isEmpty()) {
            for (TourPackageImage img : tourpackageimages) {
                if (img.getIsMain() == 1) {
                    return img.getImageUrl();
                }
            }
            return tourpackageimages.get(0).getImageUrl();
        }
        return null;
    }
    public List<TourPackageImage> getTourpackageimages() { return tourpackageimages; }
    public void setTourpackageimages(List<TourPackageImage> tourpackageimages) {
        this.tourpackageimages = tourpackageimages;
    }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
