package com.ehpefi.iforgotthat;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ListHelper extends SQLiteOpenHelper {
	// Important constants for handling the database
	static final int VERSION = 1;
	static final String DATABASE_NAME = "ift.db";

	// Specify which class which logs messages
	private static final String TAG = "ListHelper";

	public ListHelper(Context context) {
		// Call the super class' constructor
		super(context, DATABASE_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "onCreate() was called. Creating a database...");

		// SQL to create the 'list' table
		String createListsSQL = "CREATE TABLE list (_id integer PRIMARY KEY AUTOINCREMENT NOT NULL, title varchar(255) NOT NULL, created_timestamp integer(128) NOT NULL);";

		try {
			db.execSQL(createListsSQL);
			Log.d(TAG, "The table 'list' was successfully created");
		} catch (SQLException sqle) {
			Log.e(TAG, "Invalid SQL detected", sqle);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "onUpgrade() was called, but nothing to upgrade...");
	}

}
