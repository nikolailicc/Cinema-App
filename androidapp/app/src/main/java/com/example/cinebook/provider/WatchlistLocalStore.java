package com.example.cinebook.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.example.cinebook.model.Movie;
import com.example.cinebook.provider.WatchlistContract.Entry;

import java.util.ArrayList;
import java.util.List;

/**
 * Jednostavan "wrapper" oko WatchlistProvider-a - koristi se iz Activity/Fragment-a
 * umesto da se direktno radi sa ContentResolver-om na svakom mestu.
 */
public class WatchlistLocalStore {

    private final ContentResolver resolver;

    public WatchlistLocalStore(Context context) {
        this.resolver = context.getApplicationContext().getContentResolver();
    }

    public void add(Movie movie) {
        ContentValues values = new ContentValues();
        values.put(Entry.COLUMN_MOVIE_ID, movie.getId());
        values.put(Entry.COLUMN_TITLE, movie.getTitle());
        values.put(Entry.COLUMN_IMAGE_URL, movie.getImageUrl());
        values.put(Entry.COLUMN_ADDED_AT, System.currentTimeMillis());
        resolver.insert(WatchlistContract.CONTENT_URI, values);
    }

    public void remove(long movieId) {
        resolver.delete(ContentUris.withAppendedId(WatchlistContract.CONTENT_URI, movieId), null, null);
    }

    public boolean isInWatchlist(long movieId) {
        try (Cursor c = resolver.query(
                ContentUris.withAppendedId(WatchlistContract.CONTENT_URI, movieId),
                null, null, null, null)) {
            return c != null && c.getCount() > 0;
        }
    }

    public List<Movie> getAll() {
        List<Movie> result = new ArrayList<>();
        try (Cursor c = resolver.query(WatchlistContract.CONTENT_URI, null, null, null, null)) {
            if (c != null) {
                while (c.moveToNext()) {
                    Movie m = new Movie();
                    m.setId(c.getLong(c.getColumnIndexOrThrow(Entry.COLUMN_MOVIE_ID)));
                    m.setTitle(c.getString(c.getColumnIndexOrThrow(Entry.COLUMN_TITLE)));
                    m.setImageUrl(c.getString(c.getColumnIndexOrThrow(Entry.COLUMN_IMAGE_URL)));
                    result.add(m);
                }
            }
        }
        return result;
    }
}
