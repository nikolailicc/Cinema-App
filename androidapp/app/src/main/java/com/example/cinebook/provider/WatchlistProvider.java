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
                        appendIdSelection(selection, Entry.COLUMN_MOVIE_ID + "=?"),
                        appendIdArgs(selectionArgs, String.valueOf(movieId)),
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
            rows = db.delete(Entry.TABLE_NAME,
                appendIdSelection(selection, Entry.COLUMN_MOVIE_ID + "=?"),
                appendIdArgs(selectionArgs, String.valueOf(movieId)));
        } else {
            rows = db.delete(Entry.TABLE_NAME, selection, selectionArgs);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rows;
    }

    private String appendIdSelection(String selection, String idSelection) {
        return selection == null ? idSelection : "(" + selection + ") AND " + idSelection;
    }

    private String[] appendIdArgs(String[] selectionArgs, String movieId) {
        String[] args = new String[(selectionArgs == null ? 0 : selectionArgs.length) + 1];
        if (selectionArgs != null) {
            System.arraycopy(selectionArgs, 0, args, 0, selectionArgs.length);
        }
        args[args.length - 1] = movieId;
        return args;
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
