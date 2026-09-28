package com.example.cinebook.api;

import com.example.cinebook.model.Movie;
import com.example.cinebook.model.Reservation;
import com.example.cinebook.model.User;
import com.example.cinebook.model.WatchlistEntry;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {

    @POST("auth/register")
    Call<Void> register(@Body Map<String, String> body);

    @GET("auth/me")
    Call<User> me();

    @GET("movies")
    Call<List<Movie>> getAllMovies();

    @GET("movies/{id}")
    Call<Movie> getMovieById(@Path("id") long id);

    @POST("movies")
    Call<Movie> createMovie(@Body Movie movie);

    @PUT("movies/{id}")
    Call<Movie> updateMovie(@Path("id") long id, @Body Movie movie);

    @DELETE("movies/{id}")
    Call<Void> deleteMovie(@Path("id") long id);

    @Multipart
    @POST("movies/{id}/image")
    Call<Movie> uploadImage(@Path("id") long id, @Part MultipartBody.Part file);

    @GET("watchlist")
    Call<List<WatchlistEntry>> getMyWatchlist();

    @POST("watchlist/{movieId}")
    Call<Object> addToWatchlist(@Path("movieId") long movieId, @Body Map<String, String> body);

    @DELETE("watchlist/{movieId}")
    Call<Void> removeFromWatchlist(@Path("movieId") long movieId);

    @POST("reservations")
    Call<Reservation> createReservation(@Body Map<String, Integer> body);

    @GET("reservations/my")
    Call<List<Reservation>> getMyReservations();

    @DELETE("reservations/{id}")
    Call<Void> deleteReservation(@Path("id") long id);

    @GET("ratings/{movieId}")
    Call<Map<String, Object>> getRating(@Path("movieId") long movieId);

    @POST("ratings/{movieId}")
    Call<Object> rateMovie(@Path("movieId") long movieId, @Body Map<String, Integer> body);

    @GET("comments/{movieId}")
    Call<List<Map<String, Object>>> getComments(@Path("movieId") long movieId);

    @POST("comments/{movieId}")
    Call<Object> addComment(@Path("movieId") long movieId, @Body Map<String, String> body);

    @DELETE("comments/{commentId}")
    Call<Void> deleteComment(@Path("commentId") long commentId);

    @GET("users")
    Call<List<User>> getAllUsers();

    @DELETE("users/{id}")
    Call<Void> deleteUser(@Path("id") long id);
}
