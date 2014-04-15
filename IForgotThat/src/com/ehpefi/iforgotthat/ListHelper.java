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

	// Specify which class that logs messages
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
		Log.d(TAG, "onCreate() was called. Creating the table '" + TABLE_NAME + "'...");

		// SQL to create the 'list' table
		String createListsSQL = String
				.format("CREATE TABLE %s (%s integer PRIMARY KEY AUTOINCREMENT NOT NULL, %s varchar(255) NOT NULL, %s DATETIME DEFAULT CURRENT_TIMESTAMP)",
						TABLE_NAME, COL_ID, COL_TITLE, COL_TIMESTAMP);

		// Create the table
		try {
			db.execSQL(createListsSQL);
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

		// Ask the database if the table 'list' exist
		Cursor cursor = db.rawQuery(
				String.format("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='%s'", TABLE_NAME), null);

		cursor.moveToFirst();

		// If not, create it
		if (cursor.getInt(0) == 0) {
			onCreate(db);
		}

		cursor.close();
	}

	/**
	 * Gets the total number of lists in the database
	 *
	 * @return The number of lists in the database
	 * @since 1.0
	 */
	public int numberOfLists() {
		// Create a pointer to the database
		SQLiteDatabase db = getReadableDatabase();

		// Ask the database of how many lists we have
		Cursor cursor = db.rawQuery(String.format("SELECT count(%s) FROM %s", COL_ID, TABLE_NAME), null);
		cursor.moveToFirst();

		// Get the result
		int number = cursor.getInt(0);
		cursor.close();

		return number;
	}

	/**
	 * Creates a new list in the database.
	 * 
	 * @param listTitle The title of the list
	 * @return The id of the inserted list on success, 0 on failure
	 * @since 1.0
	 */
	public int createNewList(String listTitle) {
		// Create a pointer to the database
		SQLiteDatabase db = getWritableDatabase();

		// Create the data to insert
		ContentValues values = new ContentValues();
		values.put(COL_TITLE, listTitle);
		
		// Try to save the list
		try {
			// Check for success
			long returnId = db.insertOrThrow(TABLE_NAME, null, values);

			// Check for success
			if (returnId != -1) {
				// Success
				Log.i(TAG, "The list '" + listTitle + "' was successfully saved");

				// Close the database connection
				db.close();

				return (int) returnId;
			}

			// Failure
			Log.e(TAG, "Could not save the list '" + listTitle + "'. That's all I know...");

			// Close the database connection
			db.close();

			return 0;
		} catch (SQLException sqle) {
			// Something wrong with the SQL
			Log.e(TAG, "Error in inserting the list named " + listTitle, sqle);

			// Close the database connection
			db.close();

			return 0;
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
			Log.d(TAG, "The table '" + TABLE_NAME + "' was dropped");
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
	 * @return A list of to do lists on success, an empty ArrayList on failure
	 * @since 1.0
	 */
	public ArrayList<ListObject> getAllLists(String OrderBy) {
		// Create an ArrayList to hold our list elements
		ArrayList<ListObject> lists = new ArrayList<ListObject>();
		
		// Create a pointer to the database
		SQLiteDatabase db = getReadableDatabase();

		// The SQL for selecting all lists from the database
		String sql = String.format("SELECT %s, %s, %s FROM %s ORDER BY %s", COL_ID, COL_TITLE, COL_TIMESTAMP,
				TABLE_NAME, OrderBy);

		// Cursor who points at the current record
		Cursor cursor = db.rawQuery(sql, null);

		// Iterate over the results
		while (cursor.moveToNext()) {
			try {
				lists.add(new ListObject(cursor.getInt(0), cursor.getString(1), cursor.getString(2)));
			} catch (Exception e) {
				Log.e(TAG, "Could not create ListObject in getAllLists(), the following exception was thrown", e);
			}
		}

		// Close the database connection
		cursor.close();
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
		String sql = String.format("SELECT %s, %s, %s FROM %s WHERE %s = %d", COL_ID, COL_TITLE, COL_TIMESTAMP,
				TABLE_NAME, COL_ID, id);

		// Cursor who points at the result
		Cursor cursor = db.rawQuery(sql, null);

		// As long as we have exactly one result
		if (cursor.getCount() == 1) {
			// Move to the only record
			cursor.moveToFirst();

			// Create the list object
			ListObject list = new ListObject(cursor.getInt(0), cursor.getString(1), cursor.getString(2));

			// Close the database connection
			cursor.close();
			db.close();

			// Return the list
			return list;
		}

		Log.e(TAG, "The cursor in getList() contains an unexpected value: " + cursor.getCount()
				+ ". Returning a null object!");

		// Close the database connection
		cursor.close();
		db.close();

		// Fail
		return null;
	}

	/**
	 * Deletes a list from the database
	 * 
	 * @param id The id of the list to be deleted
	 * @param context The context which is responsible for deleting
	 * @return True if success, false otherwise
	 * @since 1.0
	 */
	public boolean deleteList(int id, Context context) {
		// Create a pointer to the database
		SQLiteDatabase db = getWritableDatabase();

		// Log that we are deleting a list
		Log.i(TAG, "Deletion of list id " + id + " requested");

		// Delete individual elements within that list
		ListElementHelper listElementHelper = new ListElementHelper(context);
		ArrayList<ListElementObject> elements = listElementHelper.getListElementsForListId(id, ListElementHelper.COL_ID);
		for (ListElementObject element : elements) {
			listElementHelper.deleteListElement(element.getId());
		}
		elements = null;

		// Try to delete the list
		if (db.delete(TABLE_NAME, COL_ID + "=?", new String[] { Integer.toString(id) }) == 1) {
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

		if (db.update(TABLE_NAME, values, COL_ID + "=?", new String[] { Integer.toString(id) }) == 1) {
			Log.i(TAG, "The list with id " + id + " was successfully renamed to " + newTitle);

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
	public boolean doesListWithTitleExist(String title) {
		// Create a pointer to the database
		SQLiteDatabase db = getReadableDatabase();

		// The SQL for selecting one list from the database
		String sql = String.format("SELECT %s FROM %s WHERE %s = '%s' COLLATE NOCASE", COL_ID, TABLE_NAME, COL_TITLE,
				title);

		// Cursor who points at the result
		Cursor cursor = db.rawQuery(sql, null);

		// The title doesn't exist
		if (cursor.getCount() == 0) {
			// Close the database connection
			cursor.close();
			db.close();

			return false;
		}
		
		// Close the database connection
		cursor.close();
		db.close();

		return true;
	}

	/**
	 * Checks whether a lists exists or not
	 * 
	 * @param id The list id
	 * @return True if the list exists, otherwise false
	 * @since 1.0
	 */
	public boolean doesListWithIdExist(int id) {
		// Create a pointer to the database
		SQLiteDatabase db = getReadableDatabase();

		// Ask the database
		Cursor cursor = db.rawQuery(String.format("SELECT COUNT(*) FROM %s WHERE _id = %d", TABLE_NAME, id), null);

		cursor.moveToFirst();

		// The list doesn't exist
		if (cursor.getInt(0) == 0) {
			cursor.close();
			db.close();
			return false;
		}

		cursor.close();
		db.close();
		return true;
	}
}
