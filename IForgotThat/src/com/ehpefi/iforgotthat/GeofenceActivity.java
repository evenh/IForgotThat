package com.ehpefi.iforgotthat;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

public class GeofenceActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {
	// Map and location
	private GoogleMap map;
	private LocationClient locationClient;
	private Location currentLocation;
	private LocationRequest locationRequest;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_geofence);

		statusBar = (TextView) findViewById(R.id.statusBar);

		// Get the map from resources
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		map.setMyLocationEnabled(true);

		// Get the user's location
		locationClient = new LocationClient(this, this, this);

		// Create the LocationRequest object
		locationRequest = LocationRequest.create();
		// Use high accuracy
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		// Set the update interval to 5 seconds
		locationRequest.setInterval(UPDATE_INTERVAL);
		// Set the fastest update interval to 1 second
		locationRequest.setFastestInterval(FASTEST_INTERVAL);
	}

	@Override
	protected void onStart() {
		super.onStart();
		locationClient.connect();
	}

	@Override
	protected void onStop() {
		if (locationClient.isConnected()) {
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
	public void onConnected(Bundle arg0) {
		if (currentLocation == null) {
			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), 14.0f));
		}

		currentLocation = locationClient.getLastLocation();
		statusBar.setText("Connected! Last location: " + currentLocation.toString());

		centerMapOnUserLocation();
	}

	private void centerMapOnUserLocation() {
		if (currentLocation != null) {
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 16));
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		currentLocation = location;
		statusBar.setText("Pos updated! Location: " + currentLocation.toString());
	}

	public void backToEditing(View v) {
		onBackPressed();
	}

	@Override
	public void onDisconnected() {
		statusBar.setText("Disconnected");
	}

}
