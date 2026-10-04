package com.example.cinebook.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.cinebook.R;
import com.example.cinebook.api.ApiService;
import com.example.cinebook.api.RetrofitClient;
import com.example.cinebook.model.CinemaSettings;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CinemaSettingsActivity extends AppCompatActivity {
    private EditText name, address, latitude, longitude;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_cinema_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        name = findViewById(R.id.editCinemaName);
        address = findViewById(R.id.editCinemaAddress);
        latitude = findViewById(R.id.editCinemaLatitude);
        longitude = findViewById(R.id.editCinemaLongitude);
        findViewById(R.id.btnSaveCinema).setOnClickListener(v -> save());
        RetrofitClient.getApiService(this).getCinemaSettings().enqueue(new Callback<CinemaSettings>() {
            @Override public void onResponse(Call<CinemaSettings> call, Response<CinemaSettings> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CinemaSettings s = response.body();
                    name.setText(s.getName()); address.setText(s.getAddress());
                    latitude.setText(String.valueOf(s.getLatitude())); longitude.setText(String.valueOf(s.getLongitude()));
                }
            }
            @Override public void onFailure(Call<CinemaSettings> call, Throwable t) {
                Toast.makeText(CinemaSettingsActivity.this, "Greška pri učitavanju lokacije", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void save() {
        if (TextUtils.isEmpty(name.getText()) || TextUtils.isEmpty(address.getText())) {
            Toast.makeText(this, R.string.empty_field_error, Toast.LENGTH_SHORT).show(); return;
        }
        final double lat, lng;
        try { lat = Double.parseDouble(latitude.getText().toString()); lng = Double.parseDouble(longitude.getText().toString()); }
        catch (NumberFormatException e) { Toast.makeText(this, "Koordinate nisu ispravne", Toast.LENGTH_SHORT).show(); return; }
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            Toast.makeText(this, "Koordinate nisu u dozvoljenom opsegu", Toast.LENGTH_SHORT).show(); return;
        }
        CinemaSettings settings = new CinemaSettings();
        settings.setName(name.getText().toString().trim()); settings.setAddress(address.getText().toString().trim());
        settings.setLatitude(lat); settings.setLongitude(lng);
        RetrofitClient.getApiService(this).updateCinemaSettings(settings).enqueue(new Callback<CinemaSettings>() {
            @Override public void onResponse(Call<CinemaSettings> call, Response<CinemaSettings> response) {
                if (response.isSuccessful()) { Toast.makeText(CinemaSettingsActivity.this, R.string.save, Toast.LENGTH_SHORT).show(); finish(); }
                else Toast.makeText(CinemaSettingsActivity.this, "Lokacija nije sačuvana", Toast.LENGTH_SHORT).show();
            }
            @Override public void onFailure(Call<CinemaSettings> call, Throwable t) {
                Toast.makeText(CinemaSettingsActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
