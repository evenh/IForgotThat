package com.ehpefi.iforgotthat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ListElementHelper extends SQLiteOpenHelper {
	// Important constants for handling the database
	private static final int VERSION = 1;
	private static final String DATABASE_NAME = "ift.db";
	private static final String TABLE_NAME = "items";

	// Column names in the database
	public static final String COL_ID = "_id";
	public static final String COL_LIST_ID = "list_id";
	public static final String COL_DESCRIPTION = "description";
	public static final String COL_CREATED_TIMESTAMP = "created_timestamp";
	public static final String COL_ALARM_TIMESTAMP = "alarm_timestamp";
	public static final String COL_COMPLETED = "completed";
	public static final String COL_IMAGE = "image";

	// Specify which class that logs messages
	private static final String TAG = "ListElementHelper";

	/**
	 * Constructs a new instance of the ListElementHelper class
	 * 
	 * @param context The context in which the new instance should be created, usually 'this'.
	 * @since 1.0
	 */
	public ListElementHelper(Context context) {
		// Call the super class' constructor
		super(context, DATABASE_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "onCreate() was called. Creating the table '" + TABLE_NAME + "'...");

		// SQL to create the 'items' table
		// TODO: Mark image BLOB NOT NULL
		String createItemsSQL = String
				.format("CREATE TABLE %s (%s integer PRIMARY KEY AUTOINCREMENT NOT NULL, %s integer NOT NULL, "
						+ "%s varchar(255), %s DATETIME DEFAULT CURRENT_TIMESTAMP, %s DATETIME, %s integer(1) NOT NULL DEFAULT(0), "
						+ "%s blob,FOREIGN KEY(%s) REFERENCES list(_id))", TABLE_NAME, COL_ID, COL_LIST_ID,
						COL_DESCRIPTION, COL_CREATED_TIMESTAMP, COL_ALARM_TIMESTAMP, COL_COMPLETED, COL_IMAGE,
						COL_LIST_ID);
		
		// Create the table
		try {
			db.execSQL(createItemsSQL);
			Log.i(TAG, "The table '" + TABLE_NAME + "' was successfully created");
		} catch (SQLException sqle) {
			Log.e(TAG, "Invalid SQL detected", sqle);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "onUpgrade() was called, but nothing to upgrade...");
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		// Maybe somewhat hacky, but SQLiteOpenHelper doesn't call onCreate() for a new
		// table, just a new database completely

		// Ask the database if the table 'items' exist
		Cursor cursor = db.rawQuery(
				String.format("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='%s'", TABLE_NAME), null);

		cursor.moveToFirst();

		// If not, create it
		if (cursor.getInt(0) == 0) {
			onCreate(db);
		}

		cursor.close();
	}

	@Override
	public void onConfigure(SQLiteDatabase db) {
		// Enable foreign keys
		db.setForeignKeyConstraintsEnabled(true);
	}

	/**
	 * Inserts a new list element into the database
	 * 
	 * @param listId The list that should contain this list element
	 * @param description The user's description of this reminder
	 * @param alarm Date and time in the following format: YYYY-MM-DD HH:MM:SS
	 * @param image A raw image
	 * @return The id of the inserted list element on success, 0 on failure
	 * @since 1.0
	 */
	public int createNewListElement(int listId, String description, String alarm, byte[] image) {
		// Create a pointer to the database
		SQLiteDatabase db = getWritableDatabase();
		
		// Create the data to insert
		ContentValues values = new ContentValues();
		values.put(COL_LIST_ID, listId);
		values.put(COL_DESCRIPTION, description);
		values.put(COL_ALARM_TIMESTAMP, alarm);
		values.put(COL_IMAGE, image);

		// Try to save the list element
		try {
			// Check for success

			// Runs the query and stores the id
			long returnId = db.insertOrThrow(TABLE_NAME, null, values);

			if (returnId != -1) {
				// Success
				Log.i(TAG, "The list element with description '" + description + "' was successfully saved");

				// Close the database connection
				db.close();

				return (int) returnId;
			}

			// Failure
			Log.e(TAG, "Could not save the list element with description '" + description + "'. That's all I know...");

			// Close the database connection
			db.close();

			return 0;
		} catch (SQLiteConstraintException sqlce) {
			Log.e(TAG, "Error in inserting the list element with description " + description
					+ " due to a database constraint. Are you sure that the provided list ID exists?");

			// Close the database connection
			db.close();

			return 0;
		} catch (SQLException sqle) {
			// Something wrong with the SQL
			Log.e(TAG, "Error in inserting the list element with description '" + description + "'", sqle);

			// Close the database connection
			db.close();

			return 0;
		}
	}

	/**
	 * Deletes a list element
	 * 
	 * @param id The list element's identifier
	 * @return True on success, otherwise false
	 * @since 1.0
	 */
	public boolean deleteListElement(int id) {
		// Create a pointer to the database
		SQLiteDatabase db = getWritableDatabase();

		// Log that we are deleting a list element
		Log.i(TAG, "Deletion of list element with id " + id + " requested");

		// Try to delete the list element
		if (db.delete(TABLE_NAME, COL_ID + "=?", new String[] { Integer.toString(id) }) == 1) {
			Log.i(TAG, "List element with id " + id + " was successfully deleted");

			// Close the database connection
			db.close();

			return true;
		}

		// Couldn't delete list element
		Log.e(TAG, "Could not delete list element with id " + id);

		// Close the database connection
		db.close();

		return false;
	}

	/**
	 * Marks a list element as completed/done
	 * 
	 * @param id The element's identifier
	 * @param status True for completed, false for not completed
	 * @return True if successfull, false otherwise
	 * @since 1.0
	 */
	public boolean setListElementComplete(int id, boolean status) {
		// Create a pointer to the database
		SQLiteDatabase db = getWritableDatabase();

		// Also convert boolean to an integer for database purposes
		int statusInt = (status) ? 1 : 0;

		// Log that we are changing the status of an id
		Log.i(TAG, "Setting the status of id: " + id + " to " + status);

		// Provide new data
		ContentValues values = new ContentValues();
		values.put(COL_COMPLETED, statusInt);

		if (db.update(TABLE_NAME, values, COL_ID + "=?", new String[] { Integer.toString(id) }) == 1) {
			Log.i(TAG, "The status of id " + id + " was successfully changed to " + status);

			// Close the database connection
			db.close();

			return true;
		}

		// Error
		Log.e(TAG, "Could not change status of the list element with id " + id + " to " + status
				+ ". Does the list element exist?");

		// Close the database connection
		db.close();

		return false;
	}
}