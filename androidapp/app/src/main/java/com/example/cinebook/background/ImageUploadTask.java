package com.example.cinebook.background;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.example.cinebook.api.ApiService;
import com.example.cinebook.api.RetrofitClient;
import com.example.cinebook.model.Movie;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

public class ImageUploadTask {

    public interface UploadCallback {

        void onStart();

        void onSuccess(Movie updatedMovie);

        void onError(String message);
    }

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void upload(Context context, long movieId, Uri imageUri, UploadCallback callback) {
        mainHandler.post(callback::onStart);

        EXECUTOR.execute(() -> {
            try {
                File tempFile = copyUriToTempFile(context, imageUri);

                RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), tempFile);
                MultipartBody.Part body = MultipartBody.Part.createFormData("file", tempFile.getName(), requestFile);

                ApiService api = RetrofitClient.getApiService(context);
                Call<Movie> call = api.uploadImage(movieId, body);
                Response<Movie> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    Movie updated = response.body();
                    mainHandler.post(() -> callback.onSuccess(updated));
                } else {
                    mainHandler.post(() -> callback.onError("HTTP " + response.code()));
                }
            } catch (IOException e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    private File copyUriToTempFile(Context context, Uri uri) throws IOException {
        File tempFile = File.createTempFile("upload_", ".jpg", context.getCacheDir());
        try (InputStream in = context.getContentResolver().openInputStream(uri);
             FileOutputStream out = new FileOutputStream(tempFile)) {
            if (in == null) throw new IOException("Ne mogu da otvorim izabranu sliku");
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
        return tempFile;
    }
}
