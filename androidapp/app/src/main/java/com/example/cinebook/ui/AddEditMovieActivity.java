package com.example.cinebook.ui;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.cinebook.R;
import com.example.cinebook.api.ApiService;
import com.example.cinebook.api.RetrofitClient;
import com.example.cinebook.background.ImageUploadTask;
import com.example.cinebook.model.Movie;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEditMovieActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE = "extra_movie";

    private EditText editTitle, editDescription, editGenre, editDuration, editScreeningDate;
    private ImageView imagePreview;
    private ProgressBar progressUpload;
    private Button btnPickImage, btnSave;

    private Movie movie; // null ako je ovo kreiranje novog filma
    private Uri pickedImageUri;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    pickedImageUri = uri;
                    Glide.with(this).load(uri).centerCrop().into(imagePreview);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_movie);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        editTitle = findViewById(R.id.editTitle);
        editDescription = findViewById(R.id.editDescription);
        editGenre = findViewById(R.id.editGenre);
        editDuration = findViewById(R.id.editDuration);
        editScreeningDate = findViewById(R.id.editScreeningDate);
        imagePreview = findViewById(R.id.imagePreview);
        progressUpload = findViewById(R.id.progressUpload);
        btnPickImage = findViewById(R.id.btnPickImage);
        btnSave = findViewById(R.id.btnSave);

        movie = (Movie) getIntent().getSerializableExtra(EXTRA_MOVIE);
        if (movie != null) {
            toolbar.setTitle(R.string.edit);
            fillFields();
        } else {
            toolbar.setTitle(R.string.add_movie);
        }

        btnPickImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnSave.setOnClickListener(v -> save());
    }

    private void fillFields() {
        editTitle.setText(movie.getTitle());
        editDescription.setText(movie.getDescription());
        editGenre.setText(movie.getGenre());
        editDuration.setText(movie.getDuration() != null ? String.valueOf(movie.getDuration()) : "");
        editScreeningDate.setText(movie.getScreeningDate());
        if (movie.getImageUrl() != null && !movie.getImageUrl().isEmpty()) {
            Glide.with(this).load(movie.getImageUrl()).centerCrop().into(imagePreview);
        }
    }

    private void save() {
        String title = editTitle.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        String genre = editGenre.getText().toString().trim();
        String durationStr = editDuration.getText().toString().trim();
        String screeningDate = editScreeningDate.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(durationStr) || TextUtils.isEmpty(screeningDate)) {
            Toast.makeText(this, R.string.empty_field_error, Toast.LENGTH_SHORT).show();
            return;
        }

        Movie payload = movie != null ? movie : new Movie();
        payload.setTitle(title);
        payload.setDescription(description);
        payload.setGenre(genre);
        payload.setDuration(Integer.parseInt(durationStr));
        payload.setScreeningDate(screeningDate);

        ApiService api = RetrofitClient.getApiService(this);
        Call<Movie> call = (movie != null)
                ? api.updateMovie(movie.getId(), payload)
                : api.createMovie(payload);

        call.enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, Response<Movie> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Movie savedMovie = response.body();
                    if (pickedImageUri != null && savedMovie.getId() != null) {
                        uploadImage(savedMovie.getId());
                    } else {
                        Toast.makeText(AddEditMovieActivity.this, R.string.save, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(AddEditMovieActivity.this, "Greška pri čuvanju filma", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Movie> call, Throwable t) {
                Toast.makeText(AddEditMovieActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Upload slike se izvrsava u pozadinskom thread-u (ImageUploadTask/ExecutorService),
     * kako dugotrajno kopiranje/slanje fajla ne bi blokiralo UI thread.
     */
    private void uploadImage(long movieId) {
        new ImageUploadTask().upload(this, movieId, pickedImageUri, new ImageUploadTask.UploadCallback() {
            @Override
            public void onStart() {
                progressUpload.setVisibility(View.VISIBLE);
                btnSave.setEnabled(false);
            }

            @Override
            public void onSuccess(Movie updatedMovie) {
                progressUpload.setVisibility(View.GONE);
                Toast.makeText(AddEditMovieActivity.this, R.string.upload_success, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message) {
                progressUpload.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                Toast.makeText(AddEditMovieActivity.this, R.string.upload_failed + ": " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
