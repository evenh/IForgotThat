package com.ehpefi.iforgotthat;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Performs CRUD operations on lists in the 'I Forgot This' app.
 * 
 * @author Even Holthe
 * @since 1.0
 */

public class ListHelper extends SQLiteOpenHelper {
	// Important constants for handling the database
	private static final int VERSION = 1;
	private static final String DATABASE_NAME = "ift.db";
	private static final String TABLE_NAME = "list";

	// Column names in the database
	private static final String COL_ID = "_id";
	private static final String COL_TITLE = "title";
	private static final String COL_TIMESTAMP = "created_timestamp";
	public static final String COL_ID = "_id";
	public static final String COL_TITLE = "title";
	public static final String COL_TIMESTAMP = "created_timestamp";

	// Specify which class which logs messages
	private static final String TAG = "ListHelper";

	/**
	 * Constructs a new instance of the ListHelper class
	 * 
	 * @param context The context in which the new instance should be created, usually 'this'.
	 * @since 1.0
	 */
	public ListHelper(Context context) {
		// Call the super class' constructor
		super(context, DATABASE_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "onCreate() was called. Creating the table '" + TABLE_NAME
				+ "'...");

		// SQL to create the 'list' table
		String createListsSQL = String
				.format("CREATE TABLE %s (%s integer PRIMARY KEY AUTOINCREMENT NOT NULL, %s varchar(255) NOT NULL, %s DATETIME DEFAULT CURRENT_TIMESTAMP)",
						TABLE_NAME, COL_ID, COL_TITLE, COL_TIMESTAMP);

		// Create the table
		try {
			db.execSQL(createListsSQL);
			Log.i(TAG, "The table '" + TABLE_NAME
					+ "' was successfully created");
		} catch (SQLException sqle) {
			Log.e(TAG, "Invalid SQL detected", sqle);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "onUpgrade() was called, but nothing to upgrade...");
	}

	/**
	 * Creates a new list in the database.
	 * 
	 * @param listTitle The title of the list
	 * @return A boolean indication whether a new list was created or not
	 * @since 1.0
	 */
	public boolean createNewList(String listTitle) {
		// Create a pointer to the database
		SQLiteDatabase db = getWritableDatabase();

		// Create the data to insert
		ContentValues values = new ContentValues();
		values.put(COL_TITLE, listTitle);
		
		// Try to save the list
		try {
			// Check for success
			if (db.insertOrThrow(TABLE_NAME, null, values) != -1) {
				// Success
				Log.i(TAG, "The list '" + listTitle
						+ "' was successfully saved");

				// Close the database connection
				db.close();

				return true;
			}

			// Failure
			Log.e(TAG, "Could not save the list '" + listTitle
						+ "'. That's all I know...");

			// Close the database connection
			db.close();

			return false;
		} catch (SQLException sqle) {
			// Something wrong with the SQL
			Log.e(TAG, "Error in inserting the list named " + listTitle, sqle);

			// Close the database connection
			db.close();

			return false;
		}
	}

	/**
	 * Drops the list table completely (including all data) and re-creates a blank list table - use for
	 * testing/debugging purposes only!
	 * 
	 * @since 1.0
	 */
	public void dropAndRecreateListTable() {
		// Create a pointer to the database
		SQLiteDatabase db = getWritableDatabase();

		// Try to drop the table
		try {
			db.execSQL("DROP TABLE " + TABLE_NAME);
			Log.d(TAG, "The table '" + TABLE_NAME
 + "' was dropped");
		} catch (SQLException sqle) {
			Log.e(TAG, "Invalid SQL detected", sqle);
		}

		// Recreate
		this.onCreate(db);

		// Close the database connection
		db.close();
	}
}
