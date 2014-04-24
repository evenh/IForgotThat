package com.ehpefi.iforgotthat;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

/**
 * Takes a picture from CameraActivity and user input to create a new reminder
 * 
 * @author Even Holthe
 * @since 1.0.0
 */
public class NewReminderActivity extends Activity {
	// Helper classes for reminders
	ListHelper listHelper = new ListHelper(this);
	ListElementHelper listElementHelper = new ListElementHelper(this);

	// Editing
	private boolean editMode = false;
	private ListElementObject editObject;

	// UI fields
	private TextView listName;
	private EditText description;
	private String descriptionText;
	private TextView alarmPreview;
	private ImageView imageHolder;

	// For the database
	private int listID;
	private String listTitle;
	private Date reminder;
	private String reminderString;
	private byte[] image;

	// Used for logging
	private static final String TAG = "NewReminderActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_reminder);

		// Initialize UI elements
		description = (EditText) findViewById(R.id.reminder_title);
		listName = (TextView) findViewById(R.id.listName);
		imageHolder = (ImageView) findViewById(R.id.camera_user_image);
		alarmPreview = (TextView) findViewById(R.id.alarm_preview);

		// Set the description to an empty string by default
		descriptionText = "";

		// Check for incoming data
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			// Check for editing mode
			if (bundle.getBoolean("editMode")) {
				editMode = true;
				editObject = listElementHelper.getListElement(bundle.getInt("id"));

				// Init alarm
				Log.d(TAG, "Object: " + editObject);
				if (editObject.getAlarmAsString().equals(ListElementObject.noAlarmString)) {
					reminder = null;
				} else {
					reminder = editObject.getAlarm();
				}

				reminderString = ListElementObject.getDateAsString(reminder);
				handleAlarmPreview();

				// Init description
				descriptionText = editObject.getDescription();
				description.setText(descriptionText);

				Log.d(TAG, "Editing mode enabled");
			}

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

			// Receive image
			if (!editMode) {
				image = bundle.getByteArray("image");
			} else {
				image = editObject.getImage();
			}

			if (image != null) {
				imageHolder.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));
			}

		} else {
			Log.e(TAG, "No extras recieved!");
		}

		description.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				descriptionChanged(description.getText().toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});

		handleAlarmPreview();

		Log.d(TAG, "Current alarm saved: " + ListElementObject.getDateAsString(reminder));
	}

	/**
	 * Handles the visibility and content of the alarm preview
	 * 
	 * @since 1.0.0
	 */
	public void handleAlarmPreview() {
		// Sets the alarm preview
		if (reminder == null || reminderString == null || reminderString.equals("")) {
			alarmPreview.setVisibility(View.GONE);
		} else {
			alarmPreview.setVisibility(View.VISIBLE);
			alarmPreview.setText(reminderString);
		}
	}

	/**
	 * Pops up an alert on the trash button onClick
	 * 
	 * @param v The view
	 * @since 1.0.0
	 */
	public void thrashAndExit(View v) {
		onBackPressed();
	}

	/**
	 * Convenience method for updating the description field
	 * 
	 * @param newDescription The new description
	 * @since 1.0.0
	 */
	private void descriptionChanged(String newDescription) {
		try {
			if (newDescription.equals("") || newDescription == "") {
				descriptionText = "";
			} else {
				descriptionText = newDescription;
			}
		} catch (NullPointerException npe) {
			descriptionText = "";
		}

		Log.d(TAG, "A description entered: " + descriptionText);
	}

	/**
	 * Shows an AlertDialog containing a date and time picker
	 * 
	 * @param view The view
	 * @since 1.0.0
	 */
	public void showAlarmDialog(View view) {
		// Get the custom date+time dialog
		final LayoutInflater inflater = getLayoutInflater();
		final View dateTimeView = inflater.inflate(R.layout.datetime_dialog, null);
		// Get the UI elements for the custom dialog
		final DatePicker datePicker = (DatePicker) dateTimeView.findViewById(R.id.datePicker);
		final TimePicker timePicker = (TimePicker) dateTimeView.findViewById(R.id.timePicker);

		datePicker.setMinDate(System.currentTimeMillis() - 1000);

		// If the user has previously selected a date+time
		if (reminder != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(reminder);

			datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);
			timePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
			timePicker.setCurrentMinute(cal.get(Calendar.MINUTE));
		}

		AlertDialog alarmDialog = new AlertDialog.Builder(this).setTitle("Set alarm").setCancelable(true).setView(dateTimeView)
				.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Get the selected date + time
						Calendar cal = Calendar.getInstance();
						cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), timePicker.getCurrentHour(), timePicker.getCurrentMinute(), 0);

						reminder = new Date(cal.getTimeInMillis());
						reminderString = ListElementObject.getDateAsString(reminder);
						handleAlarmPreview();
						Log.d(TAG, "The user selected this reminder: " + ListElementObject.getDateAsString(reminder));

						dialog.dismiss();
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.d(TAG, "The user canceled the date+time dialog");
						dialog.cancel();
					}
				}).setNeutralButton(R.string.no_alarm, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.d(TAG, "The user selected 'No alarm'");
						reminder = null;
						reminderString = ListElementObject.getDateAsString(null);
						handleAlarmPreview();
						dialog.cancel();
					}
				}).create();

		alarmDialog.show();
	}

	/**
	 * Saves the item to the database
	 * 
	 * @param view The view
	 * @since 1.0.0
	 */
	public void saveReminder(View view) {
		if (!editMode) {
			Log.i(TAG, "Saving the reminder...");
		} else {
			Log.i(TAG, "Updating the reminder...");
		}

		// What data goes in
		Log.d(TAG, "List ID: " + listID);
		Log.d(TAG, "List title: " + listTitle);
		Log.d(TAG, "Description: " + descriptionText);
		Log.d(TAG, "Alarm: " + reminderString);

		// If regular mode
		if (!editMode) {
			int insertedReminder = listElementHelper.createNewListElement(listID, descriptionText, reminderString, image);

			if (insertedReminder > 0) {
				// Creates an alarm if necessary
				ListElementObject inserted = listElementHelper.getListElement(insertedReminder);
				inserted.registerAlarm(this);
			} else {
				Log.e(TAG, "Couldn't save the reminder!");
			}
		} else {
			editObject.setDescription(descriptionText);
			editObject.setAlarm(reminderString);

			Log.d(TAG, "Updated object: " + editObject.toString());
			// If updating was a success, register the alarm
			if (listElementHelper.updateListElement(editObject) != null) {
				editObject.registerAlarm(this);
			} else {
				Log.e(TAG, "Couldn't update object with id #" + editObject.getId());
			}
		}

		// Go back to the selected list
		Intent intent = new Intent(this, ListWithElementsActivity.class);
		// Clear history and pass along the list ID
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.putExtra("listID", listID);
		intent.putExtra("title", listTitle);

		startActivity(intent);
	}

	public void onBackPressed(View v) {
		onBackPressed();
	}

	@Override
	public void onBackPressed() {
		final Context context = this;

		AlertDialog confirmDeletion = new AlertDialog.Builder(this).setTitle(R.string.are_you_sure).setMessage(R.string.thrash_current_reminder)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Create an intent
						Intent intent;

						// Which activity should be fired?
						if (!editMode) {
							intent = new Intent(context, CamTest.class);
						} else {
							intent = new Intent(context, ListWithElementsActivity.class);
						}

						intent.putExtra("listID", listID);
						startActivity(intent);
						// Transition animation
						overridePendingTransition(R.anim.left_in, R.anim.right_out);
					}
				}).setNegativeButton(R.string.no, new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).create();

		confirmDeletion.show();
	}

}
