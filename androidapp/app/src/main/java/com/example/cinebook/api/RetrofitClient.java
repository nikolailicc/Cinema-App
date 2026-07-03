package com.example.cinebook.api;

import android.content.Context;
import android.util.Base64;

import com.example.cinebook.util.SessionManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // Ukoliko testiras na fizickom uredjaju/emulatoru, 10.0.2.2 mapira na localhost racunara (Android Emulator)
    private static final String BASE_URL = "http://10.0.2.2:8080/";

    private static ApiService apiService;

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
                if (username != null && password != null) {
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
