package com.ehpefi.iforgotthat;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
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

	// Milliseconds per second
	private static final int MILLISECONDS_PER_SECOND = 1000;
	// Update frequency in seconds
	public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
	// Update frequency in milliseconds
	private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
	// The fastest update frequency, in seconds
	private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
	// A fast frequency ceiling in milliseconds
	private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

	// UI
	private TextView statusBar;

	public static final String TAG = "GeofenceActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_geofence);

		// Find our status field
		statusBar = (TextView) findViewById(R.id.statusBar);

		// Geocoder
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
				LatLng dragPosition = marker.getPosition();
				double dragLat = dragPosition.latitude;
				double dragLong = dragPosition.longitude;

				String address = "No address available";

				try {
					List<Address> addresses = geocoder.getFromLocation(dragLat, dragLong, 1);
					address = addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getAddressLine(1);
				} catch (IOException e) {
				}

				// createGeofence(dragLat, dragLong, distance, "CIRCLE", "GEOFENCE");
				marker.setTitle(address);
				Toast.makeText(GeofenceActivity.this, "Address: " + address, Toast.LENGTH_SHORT).show();
				Log.i(TAG, "on drag end :" + dragLat + " dragLong :" + dragLong);
			}

			@Override
			public void onMarkerDragStart(Marker marker) {
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
		statusBar.setText("Connection failed");
	}

	@Override
	public void onConnected(Bundle bundle) {
		// Create the LocationRequest object
		locationRequest = LocationRequest.create();
		// Use high accuracy
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		// Set the update interval to 5 seconds
		locationRequest.setInterval(UPDATE_INTERVAL);
		// Set the fastest update interval to 1 second
		locationRequest.setFastestInterval(FASTEST_INTERVAL);

		locationClient.requestLocationUpdates(locationRequest, this);

		// Move camera to last known location
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), 14.0f));

		Marker stopMarker = map.addMarker(new MarkerOptions().draggable(true).position(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()))
				.title("Your current location").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher)));

		currentLocation = lastKnownLocation;
		centerMapOnUserLocation();

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
		currentLocation = location;
	}

	public void backToEditing(View v) {
		onBackPressed();
	}

	@Override
	public void onDisconnected() {
		statusBar.setText("Disconnected");
	}

}
