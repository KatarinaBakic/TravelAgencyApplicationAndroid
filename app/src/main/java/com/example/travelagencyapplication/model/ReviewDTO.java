package com.example.travelagencyapplication.model;

public class ReviewDTO {
    private String review;
    private int rating;
    private long userId;
    private long tourPackageId;

    public ReviewDTO(String review, int rating, long userId, long tourPackageId) {
        this.review        = review;
        this.rating        = rating;
        this.userId        = userId;
        this.tourPackageId = tourPackageId;
    }
    public int getRating() {
        return rating;
    }
    public void setRating(int rating) {
        this.rating = rating;
    }
    public String getReview() {
        return review;
    }
    public void setReview(String review) {
        this.review = review;
    }
    public long getTourPackageId() {
        return tourPackageId;
    }
    public void setTourPackageId(long tourPackageId) {
        this.tourPackageId = tourPackageId;
    }
    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }
}
