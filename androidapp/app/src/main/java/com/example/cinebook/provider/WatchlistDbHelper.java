package com.example.cinebook.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.cinebook.provider.WatchlistContract.Entry;

public class WatchlistDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "cinebook_local.db";
    private static final int DATABASE_VERSION = 2;

    private static final String SQL_CREATE =
            "CREATE TABLE " + Entry.TABLE_NAME + " (" +
                    Entry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Entry.COLUMN_USERNAME + " TEXT NOT NULL, " +
                    Entry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                    Entry.COLUMN_TITLE + " TEXT, " +
                    Entry.COLUMN_IMAGE_URL + " TEXT, " +
                    Entry.COLUMN_ADDED_AT + " INTEGER, " +
                    "UNIQUE(" + Entry.COLUMN_USERNAME + ", " + Entry.COLUMN_MOVIE_ID + ")" +
                    ")";

    public WatchlistDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + Entry.TABLE_NAME + " RENAME TO watchlist_legacy");
            onCreate(db);
            db.execSQL("INSERT INTO " + Entry.TABLE_NAME + " (" + Entry.COLUMN_USERNAME + ", "
                    + Entry.COLUMN_MOVIE_ID + ", " + Entry.COLUMN_TITLE + ", "
                    + Entry.COLUMN_IMAGE_URL + ", " + Entry.COLUMN_ADDED_AT + ") "
                    + "SELECT '', " + Entry.COLUMN_MOVIE_ID + ", " + Entry.COLUMN_TITLE + ", "
                    + Entry.COLUMN_IMAGE_URL + ", " + Entry.COLUMN_ADDED_AT
                    + " FROM watchlist_legacy");
            db.execSQL("DROP TABLE watchlist_legacy");
        }
    }
}
