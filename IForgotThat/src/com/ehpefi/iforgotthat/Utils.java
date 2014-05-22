package com.ehpefi.iforgotthat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

/**
 * A collection of helping methods
 * 
 * @author Even Holthe
 * @since 1.0.0
 */
public class Utils {
	private static final String TAG = "Utils";

	/**
	 * Checks whether location services is enabled. If not, the method displays a dialog prompting the user to enable location services
	 * 
	 * @param context The calling activity
	 * @return True if location services is enabled, false otherwise
	 * @since 1.0.0
	 */
	public static boolean isLocationEnabled(final Context context) {
		if (android.os.Build.VERSION.SDK_INT >= 19) {
			int locationProviders = Settings.Secure.LOCATION_MODE_OFF;

			// Fetch the location mode
			try {
				locationProviders = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
			} catch (SettingNotFoundException e) {
				Log.e(TAG, "LOCATION_MODE not found!", e);
			}

			// If we have location enabled, return true
			if (locationProviders != Settings.Secure.LOCATION_MODE_OFF) {
				return true;
			}
		} else {
			// Below API level 19
			LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			try {
				if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
					// We have location providers
					return true;
				}
			} catch (Exception ex) {
				Log.d(TAG, "Got exception while trying to fetch location providers in legacy mode", ex);
			}
		}
		// Show a dialog that location has to be enabled
		Dialog dialog = new AlertDialog.Builder(context).setTitle(R.string.no_loc_providers_enabled_title).setMessage(R.string.no_loc_providers_enabled_message)
				.setPositiveButton(R.string.no_loc_providers_enabled_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					}
				}).setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).create();

		dialog.show();

		return false;
	}
}
