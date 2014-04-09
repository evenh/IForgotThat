package com.ehpefi.iforgotthat;

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Takes a picture from CameraActivity and user input to create a new reminder
 * 
 * @author Even Holthe
 * @since 1.0
 */
public class NewReminderActivity extends Activity {
	// Helper classes for reminders
	ListHelper listHelper = new ListHelper(this);
	ListElementHelper listElementHelper = new ListElementHelper(this);

	// Data fields
	TextView listName;
	EditText title;
	Date reminder;
	Bitmap image;

	// For the database
	int listID;
	String listTitle;

	// Used for logging
	private static final String TAG = "NewReminderActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_reminder);

		title = (EditText) findViewById(R.id.reminder_title);
		listName = (TextView) findViewById(R.id.listName);

		// Check for incoming data
		Bundle bundle = getIntent().getExtras();
		if (bundle != null && bundle.getInt("listID") != 0) {
			listID = bundle.getInt("listId");
			listTitle = listHelper.getList(listID).getTitle();

			Log.d(TAG, "List id recieved: " + listID);
			Log.d(TAG, "List title recieved: " + listTitle);

			// Set the list name
			listName.setText(listTitle);
		} else {
			Log.e(TAG, "No extras recieved!");
		}
	}

	/**
	 * Pops up an alert on the thrash button onClick
	 * 
	 * @param The view
	 * @since 1.0
	 */
	public void thrashAndExit(View v) {
		AlertDialog confirmDeletion = new AlertDialog.Builder(this).setTitle("Are you sure?").setMessage("Do you want to thrash this reminder?")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						onBackPressed();
					}
				}).setNegativeButton("No", new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).create();

		confirmDeletion.show();
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, CameraActivity.class);
		startActivityForResult(intent, 0);
		// Transition animation
		overridePendingTransition(R.anim.left_in, R.anim.right_out);
	}
}
