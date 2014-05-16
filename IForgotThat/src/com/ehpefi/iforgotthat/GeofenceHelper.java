package com.ehpefi.iforgotthat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.location.Geofence;

/**
 * Performs CRUD operations on geofences in the 'I Forgot This' app.
 * 
 * @author Even Holthe
 * @since 1.0.0
 */

public class GeofenceHelper extends SQLiteOpenHelper {
	// Context
	private final Context context;

	// Important constants for handling the database
	private static final int VERSION = 1;
	private static final String DATABASE_NAME = "ift.db";
	private static final String TABLE_NAME = "geofence";

	// Column names in the database
	public static final String COL_ID = "_id";
	public static final String COL_LAT = "lat";
	public static final String COL_LON = "lon";
	public static final String COL_DISTANCE = "distance";
	public static final String COL_ADDRESS = "address";
	public static final String COL_TITLE = "title";

	// Specify which class that logs messages
	private static final String TAG = "GeofenceHelper";

	/**
	 * Constructs a new instance of the GeofenceHelper class
	 * 
	 * @param context The context in which the new instance should be created, usually 'this'.
	 * @since 1.0.0
	 */
	public GeofenceHelper(Context context) {
		// Call the super class' constructor
		super(context, DATABASE_NAME, null, VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "onCreate() was called. Creating the table '" + TABLE_NAME + "'...");

		// SQL to create the 'geofence' table
		String createGeofenceSQL = String.format(
				"CREATE TABLE %s (%s INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, %s REAL NOT NULL, %s REAL NOT NULL, %s INTEGER NOT NULL, %s TEXT NOT NULL, %s TEXT NOT NULL)",
				TABLE_NAME, COL_ID, COL_LAT, COL_LON, COL_DISTANCE, COL_ADDRESS, COL_TITLE);

		// Create the table
		try {
			db.execSQL(createGeofenceSQL);
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
	 * Inserts a new geofence to the database
	 * 
	 * @param latitude The geofence's latitude coordinate
	 * @param longitude The geofence's longitude coordinate
	 * @param distance Distance in meters where the geofence should trigger
	 * @param address Address of the coordinates
	 * @param title The user's title
	 * @return The id of the geofence in the database on success, 0 otherwise
	 * @since 1.0.0
	 */
	public int createNewGeofence(double latitude, double longitude, int distance, String address, String title) {
		// Create a pointer to the database
		SQLiteDatabase db = getWritableDatabase();

		// Create the data to insert
		ContentValues values = new ContentValues();
		values.put(COL_LAT, latitude);
		values.put(COL_LON, longitude);
		values.put(COL_DISTANCE, distance);
		values.put(COL_ADDRESS, address);
		values.put(COL_TITLE, title);

		// Try to save the list element
		try {
			// Runs the query and stores the id
			long returnId = db.insertOrThrow(TABLE_NAME, null, values);

			// Check for success
			if (returnId != -1) {
				// Success
				Log.i(TAG, "The geofence with address '" + address + "' was successfully saved");

				// Close the database connection
				db.close();

				return (int) returnId;
			}

			// Failure
			Log.e(TAG, "Could not save the geofence with address '" + address + "'. That's all I know...");

			// Close the database connection
			db.close();

			return 0;
		} catch (SQLException sqle) {
			// Something wrong with the SQL
			Log.e(TAG, "Error in inserting the geofence with address '" + address + "'", sqle);

			// Close the database connection
			db.close();

			return 0;
		}
	}

	/**
	 * Deletes an existing geofence from the database
	 * 
	 * @param id The id of the geofence
	 * @return True on success, false otherwise
	 * @since 1.0.0
	 */
	public boolean deleteGeofence(int id) {
		// Create a pointer to the database
		SQLiteDatabase db = getWritableDatabase();

		// Log that we are deleting a list element
		Log.i(TAG, "Deletion of geofence with id " + id + " requested");

		// For reminders that has this geofence, set it's geofence to 0
		ListElementHelper leh = new ListElementHelper(context);
		ArrayList<ListElementObject> reminders = leh.getItemsWithGeofence(id);
		if (reminders.size() > 0) {
			for (ListElementObject reminder : leh.getItemsWithGeofence(id)) {
				Log.d(TAG, "Resetting geofenceId to 0 for reminder with id " + reminder.getId());
				reminder.setGeofenceId(0);
				leh.updateListElement(reminder);
			}
		}

		// Try to delete the list element
		if (db.delete(TABLE_NAME, COL_ID + "=?", new String[] { Integer.toString(id) }) == 1) {
			Log.i(TAG, "Geofence with id " + id + " was successfully deleted");

			// Close the database connection
			db.close();

			return true;
		}

		// Couldn't delete list element
		Log.e(TAG, "Could not delete geofence with id " + id);

		// Close the database connection
		db.close();

		return false;
	}

	/**
	 * Gets the saved address of a geofence in the database
	 * 
	 * @param id The geofence id
	 * @return The address of the geofence on success, "No address available" otherwise
	 */
	public String getAddressForGeofence(int id) {
		String address = context.getString(R.string.no_address_available);

		// Create a pointer to the database
		SQLiteDatabase db = getReadableDatabase();

		// The SQL for selecting one geofence from the database
		String sql = String.format("SELECT %s, %s, %s, %s, %s, %s FROM %s WHERE %s = %d", COL_ID, COL_LAT, COL_LON, COL_DISTANCE, COL_ADDRESS, COL_TITLE, TABLE_NAME, COL_ID, id);

		// Cursor who points at the result
		Cursor cursor = db.rawQuery(sql, null);

		// As long as we have exactly one result
		if (cursor.getCount() == 1) {
			// Move to the only record
			cursor.moveToFirst();
			Geocoder gc = new Geocoder(context);
			try {
				List<Address> addresses = gc.getFromLocation(cursor.getDouble(1), cursor.getDouble(2), 1);
				address = addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getAddressLine(1);
			} catch (IOException e) {
			}
			// Close the database connection
			cursor.close();
			db.close();

			// Return the geofence address
			return address;
		}

		Log.e(TAG, "The cursor in getAddressForGeofence() contains an unexpected value: " + cursor.getCount() + ". Returning a null object!");

		// Close the database connection
		cursor.close();
		db.close();

		// Fail
		return null;
	}

	/**
	 * A simple class for holding geofence data
	 * 
	 * @author Even Holthe
	 * @since 1.0.0
	 */
	public class GeofenceData {
		public int id;
		public double lat;
		public double lon;
		public float distance;
		public String address;
		public String title;
	}

	/**
	 * Gets all the geofences in the database. For populating a ListView so the user can pick a single geofence
	 * 
	 * @return An array of simple GeofenceData objects on success, empty array on failure
	 * @since 1.0.0
	 */
	public ArrayList<GeofenceHelper.GeofenceData> getAllGeofences() {
		// Create a pointer to the database
		SQLiteDatabase db = getReadableDatabase();

		// The SQL for selecting all geofences from the database
		String sql = String.format("SELECT %s, %s, %s, %s, %s, %s FROM %s", COL_ID, COL_LAT, COL_LON, COL_DISTANCE, COL_ADDRESS, COL_TITLE, TABLE_NAME);

		// Cursor who points at the result
		Cursor cursor = db.rawQuery(sql, null);

		// Array to hold our data
		ArrayList<GeofenceHelper.GeofenceData> returnData = new ArrayList<GeofenceHelper.GeofenceData>();

		// As long as we have exactly one result
		if (cursor != null && cursor.getCount() >= 1) {
			// Move to the only record
			cursor.moveToFirst();

			for (int i = 0; i < cursor.getCount(); i++) {
				GeofenceData data = new GeofenceData();
				data.id = cursor.getInt(0);
				data.lat = cursor.getDouble(1);
				data.lon = cursor.getDouble(2);
				data.distance = Float.valueOf(cursor.getInt(3));
				data.address = cursor.getString(4);
				data.title = cursor.getString(5);

				returnData.add(data);

				cursor.moveToNext();
			}

			// Close the database connection
			cursor.close();
			db.close();

			// Return the geofence
			return returnData;
		}

		Log.e(TAG, "The cursor in getGeofence() contains an unexpected value: " + cursor.getCount() + ". Returning a null object!");

		// Close the database connection
		cursor.close();
		db.close();

		// Fail
		return null;
	}

	/**
	 * Gets a specific geofence
	 * 
	 * @param id The id of the geofence from the database
	 * @param reminderId The id of the ListElementObject
	 * @return An Android Geofence object
	 * @since 1.0.0
	 */
	public Geofence getGeofence(int id, int reminderId) {
		// Create a pointer to the database
		SQLiteDatabase db = getReadableDatabase();

		// The SQL for selecting one geofence from the database
		String sql = String.format("SELECT %s, %s, %s, %s, %s, %s FROM %s WHERE %s = %d", COL_ID, COL_LAT, COL_LON, COL_DISTANCE, COL_ADDRESS, COL_TITLE, TABLE_NAME, COL_ID, id);

		// Cursor who points at the result
		Cursor cursor = db.rawQuery(sql, null);

		// As long as we have exactly one result
		if (cursor.getCount() == 1) {
			// Move to the only record
			cursor.moveToFirst();

			Geofence fence = new Geofence.Builder().setRequestId("GEO" + reminderId).setCircularRegion(cursor.getDouble(1), cursor.getDouble(2), Float.valueOf(cursor.getInt(3)))
					.setExpirationDuration(Geofence.NEVER_EXPIRE).setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER).build();

			// Close the database connection
			cursor.close();
			db.close();

			// Return the geofence
			return fence;
		}

		Log.e(TAG, "The cursor in getGeofence() contains an unexpected value: " + cursor.getCount() + ". Returning a null object!");

		// Close the database connection
		cursor.close();
		db.close();

		// Fail
		return null;
	}
}
