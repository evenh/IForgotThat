package com.ehpefi.iforgotthat;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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

	/**
	 * Returns all the lists existing in the database
	 * 
	 * @param OrderBy A static string from the ListHelper class (COL_ID, COL_TITLE, COL_TIMESTAMP)
	 * @return A list of to do lists
	 * @since 1.0
	 */
	public ArrayList<ListObject> getAllLists(String OrderBy) {
		// Create an ArrayList to hold our list elements
		ArrayList<ListObject> lists = new ArrayList<ListObject>();
		
		// Create a pointer to the database
		SQLiteDatabase db = getReadableDatabase();

		// The SQL for selecting all lists from the database
		String sql = String.format("SELECT %s, %s, %s FROM %s ORDER BY %s",
				COL_ID, COL_TITLE, COL_TIMESTAMP, TABLE_NAME,
				OrderBy);

		// Cursor who points at the current record
		Cursor cursor = db.rawQuery(sql, null);

		// Iterate over the results
		while (cursor.moveToNext()) {
			try {
				lists.add(new ListObject(cursor.getInt(0), cursor.getString(1),
						cursor.getString(2)));
			} catch (Exception e) {
				Log.e(TAG,
						"Could not create ListObject in getAllLists(), the following exception was thrown",
						e);
			}
		}

		// Close the database connection
		db.close();

		// Return the list
		return lists;
	}
	
	/**
	 * Retrieves one list from the database based on the input id
	 * 
	 * @param id The id of the list from the database
	 * @return A ListObject if successful, null on failure
	 * @since 1.0
	 */
	public ListObject getList(int id) {
		// Create a pointer to the database
		SQLiteDatabase db = getReadableDatabase();

		// The SQL for selecting one list from the database
		String sql = String.format("SELECT %s, %s, %s FROM %s WHERE %s = %d",
				COL_ID, COL_TITLE, COL_TIMESTAMP, TABLE_NAME, COL_ID, id);

		// Cursor who points at the result
		Cursor cursor = db.rawQuery(sql, null);

		// As long as we have exactly one result
		if (cursor.getCount() == 1) {
			// Move to the only record
			cursor.moveToFirst();

			// Create the list object
			ListObject list = new ListObject(cursor.getInt(0),
					cursor.getString(1), cursor.getString(2));

			// Close the database connection
			db.close();

			// Return the list
			return list;
		}

		Log.e(TAG, "The cursor in getList() contains an unexpected value: "
				+ cursor.getCount() + ". Returning a null object!");

		// Close the database connection
		db.close();

		// Fail
		return null;
	}

	/**
	 * Deletes a list from the database
	 * 
	 * @param id The id of the list to be deleted
	 * @return True if success, false otherwise
	 * @since 1.0
	 */
	public boolean deleteList(int id) {
		// Create a pointer to the database
		SQLiteDatabase db = getWritableDatabase();

		// Log that we are deleting a list
		Log.i(TAG, "Deletion of list id " + id + " requested");

		// Try to delete the list
		if (db.delete(TABLE_NAME, COL_ID + "=?",
				new String[] { Integer.toString(id) }) == 1) {
			Log.i(TAG, "List with id " + id + " was successfully deleted");

			// Close the database connection
			db.close();

			return true;
		}

		// Couldn't delete list
		Log.e(TAG, "Could not delete list with id " + id);

		// Close the database connection
		db.close();

		return false;
	}

	/**
	 * Renames an existing list
	 * 
	 * @param id The id of the existing list
	 * @param newTitle The new title of the existing list
	 * @return True if success, false otherwise
	 * @since 1.0
	 */
	public boolean renameList(int id, String newTitle) {
		// Create a pointer to the database
		SQLiteDatabase db = getWritableDatabase();

		// Log that we are renaming a list
		Log.i(TAG, "Renaming the list with id: " + id + " to " + newTitle);

		// Provide new data
		ContentValues values = new ContentValues();
		values.put(COL_TITLE, newTitle);

		if (db.update(TABLE_NAME, values, COL_ID + "=?",
				new String[] { Integer.toString(id) }) == 1) {

			Log.i(TAG, "The list with id " + id
					+ " was successfully renamed to " + newTitle);

			// Close the database connection
			db.close();

			return true;
		}

		// Couldn't rename the list
		Log.e(TAG, "Could not rename the list with id " + id);

		// Close the database connection
		db.close();

		return false;
	}

	/**
	 * Checks whether a list title is unique
	 * 
	 * @param title The list title to check if is unique (case insensitive)
	 * @return True if the title is unique (not used), false otherwise
	 * @since 1.0
	 */
	public boolean isTitleUnique(String title) {
		// Create a pointer to the database
		SQLiteDatabase db = getReadableDatabase();

		// The SQL for selecting one list from the database
		String sql = String.format(
				"SELECT %s FROM %s WHERE %s = '%s' COLLATE NOCASE", COL_ID,
				TABLE_NAME, COL_TITLE, title);

		// Cursor who points at the result
		Cursor cursor = db.rawQuery(sql, null);

		// The title doesn't exist
		if (cursor.getCount() == 0) {
			// Close the database connection
			db.close();

			return true;
		}
		
		// Close the database connection
		db.close();

		return false;
	}
}
