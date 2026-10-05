package com.example.cinebook.api;

import android.content.Context;
import android.util.Base64;

import com.example.cinebook.util.SessionManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import com.example.cinebook.BuildConfig;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = BuildConfig.API_BASE_URL.endsWith("/")
            ? BuildConfig.API_BASE_URL
            : BuildConfig.API_BASE_URL + "/";

    private static ApiService apiService;

    public static String absoluteImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty() || imageUrl.startsWith("http://")
                || imageUrl.startsWith("https://")) {
            return imageUrl;
        }
        return BASE_URL + (imageUrl.startsWith("/") ? imageUrl.substring(1) : imageUrl);
    }

    public static ApiService getApiService(Context context) {
        if (apiService == null) {
            SessionManager session = new SessionManager(context);

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            Interceptor authInterceptor = chain -> {
                Request original = chain.request();
                Request.Builder builder = original.newBuilder();

                String username = session.getUsername();
                String password = session.getPassword();
                boolean isPublicRegistration = original.url().encodedPath().equals("/auth/register");
                if (!isPublicRegistration && username != null && password != null) {
                    String credentials = username + ":" + password;
                    String basic = "Basic " + Base64.encodeToString(
                            credentials.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
                    builder.header("Authorization", basic);
                }
                return chain.proceed(builder.build());
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .addInterceptor(logging)
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }
}
