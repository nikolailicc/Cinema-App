package com.example.cinebook.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.cinebook.R;

public class CinemaMapActivity extends AppCompatActivity {

    private static final double CINEMA_LAT = 44.8206;
    private static final double CINEMA_LNG = 20.4587;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        WebView webView = findViewById(R.id.webViewMap);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("file:///android_asset/map.html");

   }

    private void openNavigation() {
        Uri geoUri = Uri.parse("geo:" + CINEMA_LAT + "," + CINEMA_LNG
                + "?q=" + CINEMA_LAT + "," + CINEMA_LNG + "(" + getString(R.string.cinema_name) + ")");
        Intent intent = new Intent(Intent.ACTION_VIEW, geoUri);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Uri browserUri = Uri.parse("https://www.openstreetmap.org/directions?to="
                    + CINEMA_LAT + "%2C" + CINEMA_LNG);
            startActivity(new Intent(Intent.ACTION_VIEW, browserUri));
        }
    }
}
