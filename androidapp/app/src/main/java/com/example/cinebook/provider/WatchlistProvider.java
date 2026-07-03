package com.example.cinebook.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cinebook.provider.WatchlistContract.Entry;

/**
 * Content Provider koji lokalno (offline) cuva filmove dodate na watchlist.
 * Kada korisnik doda/ukloni film sa watchlist-e (REST poziv ka backendu),
 * app istovremeno azurira i ovaj lokalni provider, tako da se watchlist
 * moze pregledati i bez internet konekcije.
 */
public class WatchlistProvider extends ContentProvider {

    private static final int WATCHLIST = 1;
    private static final int WATCHLIST_ID = 2;

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(WatchlistContract.AUTHORITY, "watchlist", WATCHLIST);
        URI_MATCHER.addURI(WatchlistContract.AUTHORITY, "watchlist/#", WATCHLIST_ID);
    }

    private WatchlistDbHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new WatchlistDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                         @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int match = URI_MATCHER.match(uri);
        Cursor cursor;
        switch (match) {
            case WATCHLIST:
                cursor = db.query(Entry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder != null ? sortOrder : Entry.COLUMN_ADDED_AT + " DESC");
                break;
            case WATCHLIST_ID:
                long movieId = ContentUris.parseId(uri);
                cursor = db.query(Entry.TABLE_NAME, projection,
                        Entry.COLUMN_MOVIE_ID + "=?", new String[]{String.valueOf(movieId)},
                        null, null, null);
                break;
            default:
                throw new IllegalArgumentException("Nepoznat URI: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = db.insertWithOnConflict(Entry.TABLE_NAME, null, values,
                SQLiteDatabase.CONFLICT_REPLACE);
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(WatchlistContract.CONTENT_URI, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int match = URI_MATCHER.match(uri);
        int rows;
        if (match == WATCHLIST_ID) {
            long movieId = ContentUris.parseId(uri);
            rows = db.delete(Entry.TABLE_NAME, Entry.COLUMN_MOVIE_ID + "=?",
                    new String[]{String.valueOf(movieId)});
        } else {
            rows = db.delete(Entry.TABLE_NAME, selection, selectionArgs);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                       @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.update(Entry.TABLE_NAME, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return rows;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = URI_MATCHER.match(uri);
        if (match == WATCHLIST) {
            return "vnd.android.cursor.dir/vnd.com.example.cinebook.watchlist";
        } else if (match == WATCHLIST_ID) {
            return "vnd.android.cursor.item/vnd.com.example.cinebook.watchlist";
        }
        throw new IllegalArgumentException("Nepoznat URI: " + uri);
    }
}
