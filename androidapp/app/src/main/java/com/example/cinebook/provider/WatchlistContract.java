package com.example.cinebook.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Definise strukturu tabele i URI-jeve za WatchlistProvider (lokalni offline watchlist).
 */
public final class WatchlistContract {

    public static final String AUTHORITY = "com.example.cinebook.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/watchlist");

    private WatchlistContract() {
    }

    public static final class Entry implements BaseColumns {
        public static final String TABLE_NAME = "watchlist";
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_IMAGE_URL = "image_url";
        public static final String COLUMN_ADDED_AT = "added_at";
    }
}
