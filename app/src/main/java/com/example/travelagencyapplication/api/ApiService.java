package com.example.travelagencyapplication.api;

import com.example.travelagencyapplication.model.CategoryDTO;
import com.example.travelagencyapplication.model.Friendship;
import com.example.travelagencyapplication.model.ReviewDTO;
import com.example.travelagencyapplication.model.TourPackageCategory;
import com.example.travelagencyapplication.model.TourPackageDTO;
import com.example.travelagencyapplication.model.User;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("users/api/login")
    Call<String> login(@Body User loginRequest);

    @GET("users/api/tourPackages")
    Call<List<TourPackageDTO>> getTourPackages(
            @Query("action") String action,   // "active", "deleted"
            @Query("categoryId") Long categoryId
    );

    @GET("tours/tourPackageDetails")
    Call<TourPackageDTO> getDetails(@Query("packageTourId") int id);

    @POST("reviews/api/add")
    Call<ResponseBody> postReview(@Body ReviewDTO review);

    @GET("users/api/details/{id}")
    Call<User> getUserByUserId(@Path("id") Long id);

    @GET("categories")
    Call<List<TourPackageCategory>> getAllCategories();

    @GET("tours/api/favourites")
    Call<List<TourPackageDTO>> getFavoritePackages(
            @Query("userId") Long userId,
            @Query("idCategory") Long categoryId
    );

    @GET("friend/api/pending/sent/{userId}")
    Call<List<Friendship>> getSentRequests(@Path("userId") long userId);

    @GET("friend/api/pending/received/{userId}")
    Call<List<Friendship>> getReceivedRequests(@Path("userId") long userId);

    @GET("friend/api/list/{userId}")
    Call<List<User>> getActiveFriends(@Path("userId") long userId);

    @GET("friend/api/available/{userId}")
    Call<List<User>> getAvailableUsers(@Path("userId") long userId);

    @POST("friend/api/accept")
    Call<Void> acceptRequest(@Query("requestId") long requestId);

    @POST("friend/api/send-request")
    Call<Void> sendRequest(@Query("senderId") long senderId, @Query("receiverId") long receiverId);

    @POST("friend/api/remove") // Tvoja postojeća metoda za brisanje
    Call<Void> removeFriend(@Query("requestId") long id);

    @POST("friend/api/remove-friendship")
    Call<Void> removeActiveFriend(
            @Query("userId") long userId,
            @Query("friendId") long friendId
    );

    @POST("friend/api/reject")
    Call<Void> rejectRequest(@Query("requestId") long id);

    @GET("tours/api/availableDestinations")
    Call<List<CategoryDTO>> getAvailableDestinations();

    @FormUrlEncoded
    @POST("tours/api/deletedDestination")
    Call<Map<String, Object>> deleteDestination(@Field("destinationId") Long id);

    @FormUrlEncoded
    @POST("tours/api/restoreDestination")
    Call<Map<String, Object>> restorePackage(@Field("destinationId") Long id);

    @GET("reviews/api/tourpackage/{tourPackageId}")
    Call<List<ReviewDTO>> getReviewsForPackage(@Path("tourPackageId") int id);

    @POST("users/api/updateProfile/{id}")
    Call<User> updateProfile(
            @Path("id") long id,
            @Body User user,
            @Query("password") String password,
            @Query("password2") String password2
    );

    @Multipart
    @POST("tours/api/updateDestination")
    Call<Map<String, Object>> updateTourPackage(
            @Part("id") RequestBody id,
            @Part("title") RequestBody title,
            @Part("description") RequestBody description,
            @Part("price") RequestBody price,
            @Part("categoryId") RequestBody categoryId,
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude,
            @Part MultipartBody.Part imageFile
    );

    @Multipart
    @POST("categories/api/add")
    Call<TourPackageCategory> addCategory(
            @Part("name") RequestBody name,
            @Part("description") RequestBody description,
            @Part MultipartBody.Part imageCat
    );

    @Multipart
    @POST("tours/api/createNew")
    Call<ResponseBody> addDestination(
            @Part("title") RequestBody title,
            @Part("description") RequestBody description,
            @Part("price") RequestBody price,
            @Part("categoryId") RequestBody categoryId,
            @Part("userId") RequestBody userId,
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude,
            @Part MultipartBody.Part image
    );

    @FormUrlEncoded
    @POST("users/api/forgotPassword")
    Call<Map<String, String>> forgotPassword(@Field("email") String email);
}
