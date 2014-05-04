package com.ehpefi.iforgotthat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
	private TextView description;
	private TextView alarmText;
	private ImageView image;

	// Data fields
	private int id;
	private int listID;
	private ListElementObject reminder;

	// Helper
	private ListElementHelper listElementHelper;

	// For logging
	public static final String TAG = "DetailedReminderActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Standard initialization
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detailed_reminder);

		// Init helper
		listElementHelper = new ListElementHelper(this);

		// UI init
		description = (TextView) findViewById(R.id.description);
		image = (ImageView) findViewById(R.id.reminder_image);
		alarmText = (TextView) findViewById(R.id.alarm_text);

		onNewIntent(getIntent());
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

			// Image
			image.setImageBitmap(reminder.getImageAsBitmap());

			// Description
			if (!reminder.getDescription().equals("")) {
				description.setText(reminder.getDescription());
			} else {
				description.setText(R.string.has_no_description);
			}

			// Alarm
			if (reminder.getAlarmAsString().equals(ListElementObject.noAlarmString)) {
				alarmText.setVisibility(View.GONE);
			} else {
				alarmText.setText(reminder.getAlarmAsString());
			}
		}
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
