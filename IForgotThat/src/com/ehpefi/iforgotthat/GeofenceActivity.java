package com.ehpefi.iforgotthat;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Handles geofencing and displaying the user's current position
 * 
 * @author Even Holthe
 * @since 1.0.0
 */
public class GeofenceActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {
	// Map and location
	private GoogleMap map;
	private LocationClient locationClient;
	private Location currentLocation;
	private LocationRequest locationRequest;
	private Geocoder geocoder;

	private Marker userGeofence;
	private Circle userGeofenceCircle;
	private static final int GEOFENCE_MAX_RADIUS_IN_METERS = 200;
	private int currentGeofenceRadius = 50;

	// Milliseconds per second
	private static final int MILLISECONDS_PER_SECOND = 1000;
	// Update frequency in seconds
	public static final int UPDATE_INTERVAL_IN_SECONDS = 30;
	// Update frequency in milliseconds
	private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
	// The fastest update frequency, in seconds
	private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
	// A fast frequency ceiling in milliseconds
	private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

	// UI
	private Button saveGeofence;
	private SeekBar geofenceRadius;
	// Notification
	Toast toast;

	public static final String TAG = "GeofenceActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_geofence);

		// Find our save button
		saveGeofence = (Button) findViewById(R.id.saveGeofenceButton);
		saveGeofence.setText(String.format(getResources().getString(R.string.save_geofence), currentGeofenceRadius));

		// Find our progress bar
		geofenceRadius = (SeekBar) findViewById(R.id.geofenceRadius);
		geofenceRadius.setMax(GEOFENCE_MAX_RADIUS_IN_METERS);
		geofenceRadius.setProgress(currentGeofenceRadius);
		geofenceRadius.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				currentGeofenceRadius = progress;
				saveGeofence.setText(String.format(getResources().getString(R.string.save_geofence), currentGeofenceRadius));
				if (userGeofenceCircle != null) {
					userGeofenceCircle.setRadius(currentGeofenceRadius);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		// Reversing coordinates to addresses
		geocoder = new Geocoder(this);

		// Get the map from resources
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		map.setMyLocationEnabled(true);
		// Whenever the marker is moved
		map.setOnMarkerDragListener(new OnMarkerDragListener() {
			@Override
			public void onMarkerDrag(Marker marker) {
			}

			@Override
			public void onMarkerDragEnd(Marker marker) {
				// Get position
				LatLng dragPosition = marker.getPosition();
				double dragLat = dragPosition.latitude;
				double dragLong = dragPosition.longitude;

				// Set initial address to an error message
				String address = getResources().getString(R.string.no_address_available);

				// Check for Internet connectivity
				ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				if (cm.getActiveNetworkInfo() != null) {
					// Fetch the address via the geocoder
					try {
						List<Address> addresses = geocoder.getFromLocation(dragLat, dragLong, 1);
						address = addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getAddressLine(1);
					} catch (IOException e) {
					}
				}

				// Update the marker's title
				marker.setTitle(address);
				userGeofence = marker;
				Toast.makeText(GeofenceActivity.this, "Address: " + address, Toast.LENGTH_SHORT).show();

				drawCircle();
			}

			@Override
			public void onMarkerDragStart(Marker marker) {
				userGeofenceCircle.remove();
			}

		});

		// Get the user's location
		locationClient = new LocationClient(this, this, this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		locationClient.connect();
	}

	@Override
	protected void onStop() {
		if (locationClient.isConnected()) {
			// Clear the marker from the map to prevent multiple markers
			map.clear();
			locationClient.removeLocationUpdates(this);
		}
		locationClient.disconnect();
		super.onStop();
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.e(TAG, "Connection failed to LocationClient");
	}

	@Override
	public void onConnected(Bundle bundle) {
		// Create the LocationRequest object
		locationRequest = LocationRequest.create();
		// Use high accuracy
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		// Trigger again after 200m movement
		locationRequest.setSmallestDisplacement(200);
		// Set the update interval to 5 seconds
		locationRequest.setInterval(UPDATE_INTERVAL);
		// Set the fastest update interval to 1 second
		locationRequest.setFastestInterval(FASTEST_INTERVAL);

		locationClient.requestLocationUpdates(locationRequest, this);

		// Display a toast that the we are currently fetching a location
		displayToast(getResources().getString(R.string.getting_location));
	}

	/**
	 * Saves the actual geofence
	 * 
	 * @param v The view
	 * @since 1.0.0
	 */
	public void saveGeofence(View v) {
		Log.d(TAG, "Geofence saving requested");
		final GeofenceHelper gfHelper = new GeofenceHelper(this);

		// Reusing rename_list dialog for entering a new name
		View nameLayout = getLayoutInflater().inflate(R.layout.rename_list, null);
		// Set text inside the EditText (a hint so the user can specify a geofence name)
		((EditText) nameLayout.findViewById(R.id.new_list_name)).setHint(R.string.name_geofence_hint);

		AlertDialog nameDialog = new AlertDialog.Builder(this).setView(nameLayout).setTitle(R.string.name_geofence)
				.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
					@Override
					// When the user has pressed "Rename"
					public void onClick(DialogInterface dialog, int id) {
						// Get the input text (the new title)
						EditText geofenceInput = (EditText) ((AlertDialog) dialog).findViewById(R.id.new_list_name);
						String geofenceName = geofenceInput.getText().toString();

						// Check for empty input
						if (geofenceName.equals("") || geofenceName == null || geofenceName.trim().length() == 0) {
							displayToast(getResources().getString(R.string.name_geofence_empty));
						} else {
							// Save the geofence and return
							int geofenceId = gfHelper.createNewGeofence(userGeofence.getPosition().latitude, userGeofence.getPosition().longitude, currentGeofenceRadius,
									userGeofence.getTitle(), geofenceName);

							Log.d(TAG, "Saved geofence has ID " + geofenceId);

							// Create an empty intent, just to carry data back to NewReminderActivity
							Intent resultIntent = new Intent();
							resultIntent.putExtra("geofenceId", geofenceId);
							setResult(Activity.RESULT_OK, resultIntent);
							finish();
						}
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					// Cancel button
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				}).create();

		nameDialog.show();
	}

	/**
	 * Animates the camera to a new location
	 * 
	 * @since 1.0.0
	 */
	private void centerMapOnUserLocation() {
		if (currentLocation != null) {
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 16));
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "New location found!");
		currentLocation = location;

		// Clear the marker from the map to prevent multiple markers
		map.clear();

		// Move camera to last known location
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 14.0f));

		// Get current address
		String address = getResources().getString(R.string.no_address_available);
		try {
			List<Address> addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
			address = addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getAddressLine(1);
		} catch (IOException e) {
		}

		userGeofence = map.addMarker(new MarkerOptions().draggable(true).position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())).title(address)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher)));

		drawCircle();
		centerMapOnUserLocation();
	}

	@Override
	public void onDisconnected() {
		Log.d(TAG, "LocationClient disconnected");
	}

	/**
	 * Convenience method for drawing circles around a marker
	 * 
	 * @since 1.0.0
	 */
	private void drawCircle() {
		// Handle circles around the marker
		if (userGeofence != null) {
			LatLng markerPosition = userGeofence.getPosition();

			CircleOptions circleOptions = new CircleOptions().center(markerPosition).radius(currentGeofenceRadius).fillColor(Color.TRANSPARENT).strokeColor(Color.BLUE)
					.strokeWidth(8);
			userGeofenceCircle = map.addCircle(circleOptions);
		}
	}

	/**
	 * Convenience method for triggering onBackPressed()
	 * 
	 * @param v
	 * @since 1.0.0
	 */
	public void onBackPressed(View v) {
		onBackPressed();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.left_in, R.anim.right_out);
	}

	/**
	 * Convenience method for displaying a toast message
	 * 
	 * @param message The message to display
	 * @since 1.0.0
	 */
	private void displayToast(String message) {
		// If there is an active toast, cancel it
		if (toast != null) {
			toast.cancel();
		}

		// Create a toast
		toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
		// Set the position to right above the keyboard (on Nexus S at least)
		toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, -50);
		toast.show();
	}

}
