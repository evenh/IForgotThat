package com.ehpefi.iforgotthat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Displays a reminder with a bigger image
 * 
 * @author Even Holthe
 * @since 1.0.0
 */
public class DetailedReminderActivity extends Activity {
	// UI
	private TextView title;
	private TextView description;
	private TextView alarmText;
	private TextView geolocation;
	private ImageView image;

	// Data fields
	private int id;
	private int listID;
	private ListElementObject reminder;

	// Helper
	private ListElementHelper listElementHelper;
	private ListHelper listHelper;
	private String desc;


	// For logging
	public static final String TAG = "DetailedReminderActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Standard initialization
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detailed_reminder);

		// Init helper
		listElementHelper = new ListElementHelper(this);
		listHelper = new ListHelper(this);

		// UI init
		title = (TextView) findViewById(R.id.detailed_view_title);
		description = (TextView) findViewById(R.id.description);
		image = (ImageView) findViewById(R.id.reminder_image);
		alarmText = (TextView) findViewById(R.id.alarm_text);
		geolocation = (TextView) findViewById(R.id.geolocation_text);

		onNewIntent(getIntent());

		// set the imageholder size to match the device
		ViewGroup.LayoutParams layoutParams = image.getLayoutParams();
		Log.d(TAG, "Width before " + layoutParams.width);

		layoutParams.width = this.getDeviceWidth();
		layoutParams.height = this.getDeviceWidth();

		image.setLayoutParams(layoutParams);
		layoutParams = image.getLayoutParams();
		Log.d(TAG, "Width after " + layoutParams.width);
	}

	@Override
	public void onNewIntent(Intent intent) {
		// Get the intent and its content
		Bundle bundle = intent.getExtras();

		// Get extras
		if (bundle != null) {
			id = bundle.getInt("id");
			reminder = listElementHelper.getListElement(id);
			Log.d(TAG, "Got the following reminder: " + reminder.toString());
			listID = bundle.getInt("listID");
			desc = bundle.getString("desc");

			// Check for a completed item + description
			if (listID != ListElementHelper.COMPLETED_LIST_ID) {
				listID = reminder.getListId();
				title.setText(listHelper.getList(listID).getTitle());
				// check if description exists
				if (desc.trim().equals("")) {
					description.setVisibility(View.GONE);
				} else {
					description.setText(desc);
				}
			} else {
				title.setText(R.string.completed_items);
			}

			// Image
			image.setImageBitmap(reminder.getImageAsBitmap());

			// Alarm
			if (reminder.getAlarmAsString().equals(ListElementObject.noAlarmString)) {
				alarmText.setVisibility(View.GONE);
			} else {
				alarmText.setText(reminder.getAlarmAsString());
			}

			if (reminder.getGeofenceId() > 0) {
				Log.d(TAG, "Has geofence");
				GeofenceHelper gfHelper = new GeofenceHelper(this);
				geolocation.setText((gfHelper.getGeofence(reminder.getGeofenceId()).title));
			} else {
				geolocation.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * Finds the current device's display with
	 * 
	 * @return The width of the display
	 * @since 1.0.0
	 */
	public int getDeviceWidth() {
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		Log.d(TAG, "The device width is: " + size.x + "px");

		int width = size.x;

		return width;
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, ListWithElementsActivity.class);
		intent.putExtra("listID", listID);
		startActivity(intent);
		// Transition animation
		overridePendingTransition(R.anim.left_in, R.anim.right_out);
	}

	public void onBackPressed(View v) {
		onBackPressed();
	}
}
