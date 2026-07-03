package com.example.cinebook.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.cinebook.R;

/**
 * Aplikacija je vezana za jedan bioskop, pa mapa prikazuje samo tu jednu (fiksnu) lokaciju.
 * Mapa je implementirana preko Leaflet.js (OpenStreetMap) unutar WebView-a - ne zahteva
 * Google Play Services niti API kljuc, samo internet konekciju za ucitavanje tile-ova.
 * HTML/JS stranica se nalazi u app/src/main/assets/map.html.
 */
public class CinemaMapActivity extends AppCompatActivity {

    // Koordinate bioskopa - moraju biti iste kao u assets/map.html
    private static final double CINEMA_LAT = 44.8206;
    private static final double CINEMA_LNG = 20.4587; // Beograd centar

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

        findViewById(R.id.btnNavigate).setOnClickListener(v -> openNavigation());
    }

    private void openNavigation() {
        // "geo:" URI otvara podrazumevanu (bilo koju instaliranu) navigacionu aplikaciju
        Uri geoUri = Uri.parse("geo:" + CINEMA_LAT + "," + CINEMA_LNG
                + "?q=" + CINEMA_LAT + "," + CINEMA_LNG + "(" + getString(R.string.cinema_name) + ")");
        Intent intent = new Intent(Intent.ACTION_VIEW, geoUri);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // Nijedna mapa aplikacija nije instalirana - otvori rutu na OpenStreetMap u browseru
            Uri browserUri = Uri.parse("https://www.openstreetmap.org/directions?to="
                    + CINEMA_LAT + "%2C" + CINEMA_LNG);
            startActivity(new Intent(Intent.ACTION_VIEW, browserUri));
        }
    }
}
