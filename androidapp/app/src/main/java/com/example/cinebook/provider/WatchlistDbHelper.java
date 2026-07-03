package com.example.cinebook.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.cinebook.provider.WatchlistContract.Entry;

public class WatchlistDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "cinebook_local.db";
    private static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE =
            "CREATE TABLE " + Entry.TABLE_NAME + " (" +
                    Entry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Entry.COLUMN_MOVIE_ID + " INTEGER UNIQUE NOT NULL, " +
                    Entry.COLUMN_TITLE + " TEXT, " +
                    Entry.COLUMN_IMAGE_URL + " TEXT, " +
                    Entry.COLUMN_ADDED_AT + " INTEGER" +
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
        db.execSQL("DROP TABLE IF EXISTS " + Entry.TABLE_NAME);
        onCreate(db);
    }
}
