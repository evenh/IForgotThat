package com.ehpefi.iforgotthat;

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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
	private TextView listName;
	private EditText title;

	private ImageView imageHolder;

	// For the database
	private int listID;
	private String listTitle;
	private Date reminder;
	private byte[] image;

	// Used for logging
	private static final String TAG = "NewReminderActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_reminder);

		title = (EditText) findViewById(R.id.reminder_title);
		listName = (TextView) findViewById(R.id.listName);
		imageHolder = (ImageView) findViewById(R.id.camera_user_image);

		// Check for incoming data
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			// Check for list id
			if (bundle.getInt("listID") > 0) {
				listID = bundle.getInt("listID");
				Log.d(TAG, "List id recieved: " + listID);

				ListObject list = listHelper.getList(listID);
				listTitle = list.getTitle();
				Log.d(TAG, "List title resolved: " + listTitle);

				// Set the list name
				listName.setText(listTitle);
			}
			// Recieve image
			image = bundle.getByteArray("image");

			if (image != null) {
				imageHolder.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));
			}

		} else {
			Log.e(TAG, "No extras recieved!");
		}
	}

	/**
	 * Pops up an alert on the trash button onClick
	 * 
	 * @param The view
	 * @since 1.0
	 */
	public void thrashAndExit(View v) {
		AlertDialog confirmDeletion = new AlertDialog.Builder(this).setTitle("Are you sure?").setMessage("Do you want to trash this reminder?")
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
