package com.example.cinebook.ui;

import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.cinebook.R;
import com.example.cinebook.api.RetrofitClient;
import com.example.cinebook.model.CinemaSettings;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CinemaMapActivity extends AppCompatActivity {

    private double cinemaLat = 44.8206;
    private double cinemaLng = 20.4587;
    private WebView webView;
    private LocationManager locationManager;
    private LocationListener pendingLocationListener;
    private CinemaSettings cinemaSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        webView = findViewById(R.id.webViewMap);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String url) {
                updateMapSettings();
            }
        });
        webView.loadUrl("file:///android_asset/map.html");
        findViewById(R.id.btnRoute).setOnClickListener(v -> requestLocationAndRoute());
        RetrofitClient.getApiService(this).getCinemaSettings().enqueue(new Callback<CinemaSettings>() {
            @Override public void onResponse(Call<CinemaSettings> call, Response<CinemaSettings> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CinemaSettings s = response.body(); cinemaSettings = s; cinemaLat = s.getLatitude(); cinemaLng = s.getLongitude();
                    ((android.widget.TextView) findViewById(R.id.textCinemaName)).setText(s.getName());
                    ((android.widget.TextView) findViewById(R.id.textCinemaAddress)).setText(s.getAddress());
                    updateMapSettings();
                }

            }
            @Override public void onFailure(Call<CinemaSettings> call, Throwable t) { }
        });
   }

    private void updateMapSettings() {
        if (cinemaSettings == null) return;
        webView.evaluateJavascript("setCinema(" + cinemaSettings.getLatitude() + "," + cinemaSettings.getLongitude() + "," +
                "'" + escapeJs(cinemaSettings.getName()) + "','" + escapeJs(cinemaSettings.getAddress()) + "')", null);
    }

    private String escapeJs(String value) { return value.replace("\\", "\\\\").replace("'", "\\'"); }

    private void requestLocationAndRoute() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 42);
            return;
        }
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Toast.makeText(this, "Tražim trenutnu lokaciju uređaja…", Toast.LENGTH_SHORT).show();
        pendingLocationListener = location -> {
            showRoute(location);
            if (locationManager != null) {
                locationManager.removeUpdates(pendingLocationListener);
            }
        };
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, pendingLocationListener);
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, pendingLocationListener);
        }
    }

    private void showRoute(Location location) {
        webView.evaluateJavascript("showRoute(" + location.getLatitude() + "," + location.getLongitude() + ")", null);
    }
    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        boolean granted = false;
        for (int result : results) granted |= result == PackageManager.PERMISSION_GRANTED;
        if (requestCode == 42 && granted) requestLocationAndRoute();
        else if (requestCode == 42) Toast.makeText(this, R.string.location_permission_required, Toast.LENGTH_LONG).show();
    }

    private void openNavigation() {
        Uri geoUri = Uri.parse("geo:" + cinemaLat + "," + cinemaLng
                + "?q=" + cinemaLat + "," + cinemaLng + "(" + getString(R.string.cinema_name) + ")");
        Intent intent = new Intent(Intent.ACTION_VIEW, geoUri);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Uri browserUri = Uri.parse("https://www.openstreetmap.org/directions?to="
                    + cinemaLat + "%2C" + cinemaLng);
            startActivity(new Intent(Intent.ACTION_VIEW, browserUri));
        }
    }
}
