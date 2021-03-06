package com.ehpefi.iforgotthat;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Performs CRUD operations on lists elements in the 'I Forgot This' app.
 * 
 * @author Even Holthe
 * @since 1.0.0
 */
public class ListElementHelper extends SQLiteOpenHelper {
	// Context
	private final Context context;

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
	private static final String COL_IMAGE = "image";
	private static final String COL_GEOFENCE = "geofence_id";

	// Specify which class that logs messages
	private static final String TAG = "ListElementHelper";

	public static final int COMPLETED_LIST_ID = Integer.MIN_VALUE;

	/**
	 * Constructs a new instance of the ListElementHelper class
	 * 
	 * @param context The context in which the new instance should be created, usually 'this'.
	 * @since 1.0.0
	 */
	public ListElementHelper(Context context) {
		// Call the super class' constructor
		super(context, DATABASE_NAME, null, VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "onCreate() was called. Creating the table '" + TABLE_NAME + "'...");

		// SQL to create the 'items' table
		String createItemsSQL = String.format("CREATE TABLE %s (%s integer PRIMARY KEY AUTOINCREMENT NOT NULL, %s integer NOT NULL, "
				+ "%s varchar(255), %s DATETIME DEFAULT CURRENT_TIMESTAMP, %s DATETIME, %s integer(1) NOT NULL DEFAULT(0), "
				+ "%s blob NOT NULL, %s integer NOT NULL, FOREIGN KEY(%s) REFERENCES list(_id))", TABLE_NAME, COL_ID, COL_LIST_ID, COL_DESCRIPTION, COL_CREATED_TIMESTAMP,
				COL_ALARM_TIMESTAMP, COL_COMPLETED, COL_IMAGE, COL_GEOFENCE, COL_LIST_ID);

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
		Cursor cursor = db.rawQuery(String.format("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='%s'", TABLE_NAME), null);

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
	 * @since 1.0.0
	 */
	public int createNewListElement(int listId, String description, String alarm, byte[] image, int geofenceId) {
		// Create a pointer to the database
		SQLiteDatabase db = getWritableDatabase();

		// Create the data to insert
		ContentValues values = new ContentValues();
		values.put(COL_LIST_ID, listId);
		values.put(COL_DESCRIPTION, description);
		values.put(COL_ALARM_TIMESTAMP, alarm);
		values.put(COL_IMAGE, image);
		values.put(COL_GEOFENCE, geofenceId);

		// Try to save the list element
		try {
			// Runs the query and stores the id
			long returnId = db.insertOrThrow(TABLE_NAME, null, values);

			// Check for success
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
			Log.e(TAG, "Error in inserting the list element with description " + description + " due to a database constraint. Are you sure that the provided list ID exists?");

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
	 * @since 1.0.0
	 */
	public boolean deleteListElement(int id) {
		// Create a pointer to the database
		SQLiteDatabase db = getWritableDatabase();

		// Log that we are deleting a list element
		Log.i(TAG, "Deletion of list element with id " + id + " requested");

		// Cancel the alarm if it exists
		ListElementHelper helper = new ListElementHelper(context);
		ListElementObject reminder = helper.getListElement(id);
		reminder.cancelAlarm(context);
		reminder.cancelGeofence(context);
		helper = null;

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
	 * @return True if successful, false otherwise
	 * @since 1.0.0
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
		Log.e(TAG, "Could not change status of the list element with id " + id + " to " + status + ". Does the list element exist?");

		// Close the database connection
		db.close();

		return false;
	}

	/**
	 * Retrieves one list element from the database based on the input id
	 * 
	 * @param id The identifier of a list element
	 * @return A ListElementObject on success, null on failure
	 * @since 1.0.0
	 */
	public ListElementObject getListElement(int id) {
		// Create a pointer to the database
		SQLiteDatabase db = getReadableDatabase();

		// The SQL for selecting one list from the database
		String sql = String.format("SELECT %s, %s, %s, %s, %s, %s, %s, %s FROM %s WHERE %s = %d", COL_ID, COL_LIST_ID, COL_DESCRIPTION, COL_COMPLETED, COL_CREATED_TIMESTAMP,
				COL_ALARM_TIMESTAMP, COL_IMAGE, COL_GEOFENCE, TABLE_NAME, COL_ID, id);

		// Cursor who points at the result
		Cursor cursor = db.rawQuery(sql, null);

		// As long as we have exactly one result
		if (cursor.getCount() == 1) {
			// Move to the only record
			cursor.moveToFirst();

			// Get completion status -> convert to boolean
			boolean completed = (cursor.getInt(3) == 1 ? true : false);

			// Create the list object
			ListElementObject listElement = new ListElementObject(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(4), cursor.getString(5), completed,
					cursor.getBlob(6), cursor.getInt(7));

			// Close the database connection
			cursor.close();
			db.close();

			// Return the list
			return listElement;
		}

		Log.e(TAG, "The cursor in getListElement() contains an unexpected value: " + cursor.getCount() + ". Returning a null object!");

		// Close the database connection
		cursor.close();
		db.close();

		// Fail
		return null;
	}

	/**
	 * Gets all list elements for a given list id
	 * 
	 * @param id The list id
	 * @param OrderBy A static string from the ListElementHelper class (COL_ID, COL_LIST_ID, COL_DESCRIPTION, COL_COMPLETED, COL_CREATED_TIMESTAMP, COL_ALARM_TIMESTAMP)
	 * @return An ArrayList of ListElementObject on success, an empty list of these on failure
	 * @since 1.0.0
	 */
	public ArrayList<ListElementObject> getListElementsForListId(int id, String OrderBy) {
		// Create an ArrayList to hold our list elements
		ArrayList<ListElementObject> listElements = new ArrayList<ListElementObject>();

		// Create a pointer to the database
		SQLiteDatabase db = getReadableDatabase();

		// The SQL for selecting list elements matching the list id
		String sql = String.format("SELECT %s, %s, %s, %s, %s, %s, %s, %s FROM %s WHERE %s = %d", COL_ID, COL_LIST_ID, COL_DESCRIPTION, COL_COMPLETED, COL_CREATED_TIMESTAMP,
				COL_ALARM_TIMESTAMP, COL_IMAGE, COL_GEOFENCE, TABLE_NAME, COL_LIST_ID, id);

		// Cursor who points at the current record
		Cursor cursor = db.rawQuery(sql, null);

		// Iterate over the results
		while (cursor.moveToNext()) {
			// Get completion status -> convert to boolean
			boolean completed = (cursor.getInt(3) == 1 ? true : false);

			try {
				listElements.add(new ListElementObject(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(4), cursor.getString(5), completed, cursor
						.getBlob(6), cursor.getInt(7)));
			} catch (Exception e) {
				Log.e(TAG, "Could not create ListElementObject in getListElementsForListId(), the following exception was thrown", e);
			}
		}

		// Close the database connection
		cursor.close();
		db.close();

		// Return the list
		return listElements;
	}

	/**
	 * Gets all incomplete list elements for a given list id. If COMPLETED_LIST_ID is passed in, it returns a list of completed items
	 * 
	 * @param id The list id
	 * @param OrderBy A static string from the ListElementHelper class (COL_ID, COL_LIST_ID, COL_DESCRIPTION, COL_COMPLETED, COL_CREATED_TIMESTAMP, COL_ALARM_TIMESTAMP)
	 * @return An ArrayList of incomplete ListElementObject on success, an empty list of these on failure
	 * @since 1.0.0
	 */
	public ArrayList<ListElementObject> getIncompleteListElementsForListId(int id, String OrderBy) {
		if (id == COMPLETED_LIST_ID) {
			return getCompletedItems();
		}

		ArrayList<ListElementObject> incompleteElements = new ArrayList<ListElementObject>();

		for (ListElementObject listElement : getListElementsForListId(id, OrderBy)) {
			if (!listElement.isCompleted()) {
				incompleteElements.add(listElement);
			}
		}

		return incompleteElements;
	}

	/**
	 * Gets all list elements which is marked as completed
	 * 
	 * @return An ArrayList of ListElementObject which is marked as completed on success, an empty list of these on failure
	 * @since 1.0.0
	 */
	public ArrayList<ListElementObject> getCompletedItems() {
		// Create an ArrayList to hold our list elements
		ArrayList<ListElementObject> completedItems = new ArrayList<ListElementObject>();

		// Create a pointer to the database
		SQLiteDatabase db = getReadableDatabase();

		// The SQL for selecting all list elements from the database which is completed
		String sql = String.format("SELECT %s, %s, %s, %s, %s, %s, %s, %s FROM %s WHERE %s = %d", COL_ID, COL_LIST_ID, COL_DESCRIPTION, COL_COMPLETED, COL_CREATED_TIMESTAMP,
				COL_ALARM_TIMESTAMP, COL_IMAGE, COL_GEOFENCE, TABLE_NAME, COL_COMPLETED, 1);

		// Cursor who points at the current record
		Cursor cursor = db.rawQuery(sql, null);

		// Iterate over the results
		while (cursor.moveToNext()) {
			// Get completion status -> convert to boolean
			boolean completed = (cursor.getInt(3) == 1 ? true : false);

			try {
				completedItems.add(new ListElementObject(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(4), cursor.getString(5), completed, cursor
						.getBlob(6), cursor.getInt(7)));
			} catch (Exception e) {
				Log.e(TAG, "Could not create ListElementObject in getCompletedItems(), the following exception was thrown", e);
			}
		}

		// Close the database connection
		cursor.close();
		db.close();

		// Return the list
		return completedItems;
	}

	/**
	 * Gets all list elements which has a specific geofence
	 * 
	 * @return An ArrayList of ListElementObject which has a specific geofence on success, an empty list of these on failure
	 * @since 1.0.0
	 */
	public ArrayList<ListElementObject> getItemsWithGeofence(int geofenceId) {
		// Create an ArrayList to hold our list elements
		ArrayList<ListElementObject> geofenceItems = new ArrayList<ListElementObject>();

		// Create a pointer to the database
		SQLiteDatabase db = getReadableDatabase();

		// The SQL for selecting all list elements from the database which has a specific geofence
		String sql = String.format("SELECT %s, %s, %s, %s, %s, %s, %s, %s FROM %s WHERE %s = %d", COL_ID, COL_LIST_ID, COL_DESCRIPTION, COL_COMPLETED, COL_CREATED_TIMESTAMP,
				COL_ALARM_TIMESTAMP, COL_IMAGE, COL_GEOFENCE, TABLE_NAME, COL_GEOFENCE, geofenceId);

		// Cursor who points at the current record
		Cursor cursor = db.rawQuery(sql, null);

		// Iterate over the results
		while (cursor.moveToNext()) {
			// Get completion status -> convert to boolean
			boolean completed = (cursor.getInt(3) == 1 ? true : false);

			try {
				geofenceItems.add(new ListElementObject(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(4), cursor.getString(5), completed, cursor
						.getBlob(6), cursor.getInt(7)));
			} catch (Exception e) {
				Log.e(TAG, "Could not create ListElementObject in getItemsWithGeofence(), the following exception was thrown", e);
			}
		}

		// Close the database connection
		cursor.close();
		db.close();

		// Return the list
		return geofenceItems;
	}

	/**
	 * Updates the database with an updated object
	 * 
	 * @param updatedListElement The object with updated information
	 * @return On success it returns the updated object back, on failure the return value is null
	 * @since 1.0.0
	 */
	public ListElementObject updateListElement(ListElementObject updatedListElement) {
		// Check if the incoming object has the correct type
		if (!(updatedListElement instanceof ListElementObject)) {
			Log.e(TAG, "The object passed to updateListElement() was not of type ListElementObject");
			return null;
		}
		// Fetches the existing object from the database if it exists
		ListElementObject storedObject = getListElement(updatedListElement.getId());

		// If the list element doesn't exist in the database
		if (storedObject == null || !(storedObject instanceof ListElementObject)) {
			Log.w(TAG, "There is no list element in the database with id " + updatedListElement.getId());
			return null;
		}

		// If the there is no updates
		if (storedObject.equals(updatedListElement)) {
			Log.w(TAG, "The list element with id " + updatedListElement.getId() + " contains no updates. Ignoring!");
			return null;
		}

		// Provide updated data
		ContentValues values = new ContentValues();

		values.put(COL_LIST_ID, updatedListElement.getListId());
		values.put(COL_DESCRIPTION, updatedListElement.getDescription());
		values.put(COL_CREATED_TIMESTAMP, updatedListElement.getCreatedAsString());
		values.put(COL_ALARM_TIMESTAMP, updatedListElement.getAlarmAsString());
		values.put(COL_COMPLETED, (updatedListElement.isCompleted() == true ? 1 : 0));
		// If there is a new image
		if (updatedListElement.getImage() != null) {
			values.put(COL_IMAGE, updatedListElement.getImage());
		}
		values.put(COL_GEOFENCE, updatedListElement.getGeofenceId());

		// Create a pointer to the database
		SQLiteDatabase db = getWritableDatabase();

		// Try to update the list element
		if (db.update(TABLE_NAME, values, COL_ID + "=?", new String[] { Integer.toString(updatedListElement.getId()) }) == 1) {
			Log.i(TAG, "The list element with id " + updatedListElement.getId() + " was successfully updated!");

			// Close the database connection
			db.close();

			return updatedListElement;
		}

		// Couldn't update the list element
		Log.e(TAG, "Could not update the list element with id " + updatedListElement.getId());

		// Close the database connection
		db.close();

		return null;
	}
}
