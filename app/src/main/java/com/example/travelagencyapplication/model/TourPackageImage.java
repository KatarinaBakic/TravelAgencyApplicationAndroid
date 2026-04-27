package com.example.travelagencyapplication.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class TourPackageImage implements Serializable {

    private long id;
    @SerializedName("imageUrl")
    private String imageUrl;
    private int isDeleted;
    private int isMain;
    private String uploadedAt;

    public TourPackageImage() {}
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public int getIsDeleted() { return isDeleted; }
    public void setIsDeleted(int isDeleted) { this.isDeleted = isDeleted; }
    public int getIsMain() { return isMain; }
    public void setIsMain(int isMain) { this.isMain = isMain; }
    public String getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(String uploadedAt) { this.uploadedAt = uploadedAt; }
}
