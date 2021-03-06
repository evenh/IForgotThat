package com.ehpefi.iforgotthat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ehpefi.iforgotthat.GeofenceHelper.GeofenceData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

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
	private ImageButton addAlarmButton;

	// For the database
	private int listID;
	private String listTitle;
	private Date reminder;
	private String reminderString;
	private byte[] image;
	private int geofenceId;

	// Used for logging
	private static final String TAG = "NewReminderActivity";

	private Context context;

	// Geofences
	private GeofenceHelper gfHelper;
	private ArrayList<GeofenceData> gfData;
	private Dialog geofencesList;
	ArrayAdapter<GeofenceData> adapter;
	private static final int GEOFENCE_REQUEST = 42;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_reminder);

		context = this;

		// Initialize UI elements
		description = (EditText) findViewById(R.id.reminder_title);
		listName = (TextView) findViewById(R.id.listName);
		alarmPreview = (TextView) findViewById(R.id.alarm_preview);
		imageHolder = (ImageView) findViewById(R.id.camera_user_image);

		// set the imageholder size to match the device
		ViewGroup.LayoutParams layoutParams = imageHolder.getLayoutParams();
		Log.d(TAG, "Width before " + layoutParams.width);

		layoutParams.width = this.getDeviceWidth();
		layoutParams.height = this.getDeviceWidth();

		imageHolder.setLayoutParams(layoutParams);
		layoutParams = imageHolder.getLayoutParams();
		Log.d(TAG, "Width after " + layoutParams.width);

		addAlarmButton = (ImageButton) findViewById(R.id.btn_addAlarm);
		// Set the description to an empty string by default
		descriptionText = "";

		// Geofence is 0 by default
		geofenceId = 0;
		// Geofence helpers
		gfHelper = new GeofenceHelper(context);
		gfData = gfHelper.getAllGeofences();

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

				// Init geofence
				if (editObject.getGeofenceId() > 0) {
					geofenceId = editObject.getGeofenceId();
					handleAlarmPreview();
				}

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
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inPurgeable = true;

				imageHolder.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length, options));
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
		// If we don't have any alarm at all
		if (reminder == null || reminderString == null || reminderString.equals("") || geofenceId == 0) {
			alarmPreview.setVisibility(View.GONE);
		}

		// If we have a geofence
		if (geofenceId > 0) {
			alarmPreview.setVisibility(View.VISIBLE);
			alarmPreview.setText((gfHelper.getGeofence(geofenceId)).title);
			alarmPreview.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.ic_menu_mylocation, 0);
		} else if (reminder != null && !reminderString.equals("")) {
			// Local formatting
			java.text.DateFormat localizedDateFormat = android.text.format.DateFormat.getDateFormat(context);
			java.text.DateFormat localizedTimeFormat = android.text.format.DateFormat.getTimeFormat(context);

			// Time based alarm
			alarmPreview.setVisibility(View.VISIBLE);
			alarmPreview.setText(localizedTimeFormat.format(reminder) + " " + localizedDateFormat.format(reminder));
			alarmPreview.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_alarms, 0);
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
	 * Makes the user select either time or geofence based alarm
	 * 
	 * @param view The view
	 * @since 1.0.0
	 */
	public void pickAlarmType(View view) {
		// Disable the alarm button, we don't want double tapping
		addAlarmButton.setEnabled(false);

		if (!addAlarmButton.isEnabled()) {
			AlertDialog pickAlarmDialog = new AlertDialog.Builder(this).setTitle(R.string.alarm_type).setMessage(R.string.alarm_type_text).setCancelable(true)
					.setPositiveButton(R.string.alarm_time, new DialogInterface.OnClickListener() {
						// Time based alarm
						@Override
						public void onClick(DialogInterface dialog, int which) {
							addAlarmButton.setEnabled(true);
							if (geofenceId == 0) {
								showAlarmDialog();
							} else {
								// If a geofence alarm exists
								AlertDialog geofenceAlarmSet = new AlertDialog.Builder(context).setTitle(R.string.alarm_already_set)
										.setMessage(R.string.alarm_already_set_geofence).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.dismiss();
											}
										}).create();

								geofenceAlarmSet.show();
							}
							dialog.dismiss();
						}
					}).setNeutralButton(R.string.alarm_geo, new DialogInterface.OnClickListener() {
						// Geofence based alarm
						@Override
						public void onClick(DialogInterface dialog, int which) {
							addAlarmButton.setEnabled(true);
							if (reminder == null) {
								checkGpsAndShowPicker();
							} else {
								// If a time based alarm exists
								AlertDialog geofenceAlarmSet = new AlertDialog.Builder(context).setTitle(R.string.alarm_already_set).setMessage(R.string.alarm_already_set_time)
										.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.dismiss();
											}
										}).create();

								geofenceAlarmSet.show();
							}

							dialog.dismiss();
						}
					}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						// Cancel
						@Override
						public void onClick(DialogInterface dialog, int which) {
							addAlarmButton.setEnabled(true);
							dialog.cancel();
						}
					}).create();

			pickAlarmDialog.show();
		}
	}

	/**
	 * Shows an AlertDialog containing a date and time picker
	 * 
	 * @param view The view
	 * @since 1.0.0
	 */
	public void showAlarmDialog() {
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
		Log.d(TAG, "GeofenceID: " + geofenceId);

		// If regular mode
		if (!editMode) {
			int insertedReminder = listElementHelper.createNewListElement(listID, descriptionText, reminderString, image, geofenceId);

			if (insertedReminder > 0) {
				// Creates an alarm if necessary
				ListElementObject inserted = listElementHelper.getListElement(insertedReminder);
				inserted.registerAlarm(this);
				// Add geofence alarm
				if (geofenceId > 0) {
					inserted.registerGeofence(this);
				}
			} else {
				Log.e(TAG, "Couldn't save the reminder!");
			}
		} else {
			editObject.setDescription(descriptionText);
			editObject.setAlarm(reminderString);
			editObject.setGeofenceId(geofenceId);

			Log.d(TAG, "Updated object: " + editObject.toString());
			// If updating was a success, register the alarm
			if (listElementHelper.updateListElement(editObject) != null) {
				editObject.registerAlarm(this);
				editObject.registerGeofence(this);
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

	/**
	 * Convenience method for calling onBackPressed()
	 * 
	 * @param v The view
	 * @since 1.0.0
	 */
	public void onBackPressed(View v) {
		onBackPressed();
	}

	/**
	 * Method for checking for Google Play Services and displaying the picker
	 * 
	 * @since 1.0.0
	 */
	public void checkGpsAndShowPicker() {
		Log.d(TAG, "The user wants to set a geolocation");

		// Check for Google Play Services
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		Log.d(TAG, "Google Play Services status: " + status);

		switch (status) {
		// If Google Play Services is available and installed
			case ConnectionResult.SUCCESS:
				if (Utils.isLocationEnabled(this)) {
					showGeofencePicker();
				}
			break;

			// If available for install
			case ConnectionResult.SERVICE_MISSING:
			case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
			case ConnectionResult.SERVICE_DISABLED:
				GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
			break;

			// If everything else fails
			default:
				Toast.makeText(this, R.string.unsupported_device, Toast.LENGTH_LONG).show();
			break;
		}
	}

	/**
	 * Shows the actual geofence picker
	 * 
	 * @since 1.0.0
	 */
	private void showGeofencePicker() {
		// Force refresh of data
		gfData = gfHelper.getAllGeofences();

		// If we don't have any geofences
		if (gfData == null || gfData.size() == 0) {
			// GeofenceActivity intent
			Intent intent = new Intent(context, GeofenceActivity.class);
			intent.putExtra("listID", listID);
			intent.putExtra("title", listTitle);
			startActivityForResult(intent, GEOFENCE_REQUEST);
			overridePendingTransition(R.anim.right_in, R.anim.left_out);
		} else {
			// Get custom layout
			View geofenceListLayout = getLayoutInflater().inflate(R.layout.list_geofences, null);
			ListView gfDataHolder = (ListView) geofenceListLayout.findViewById(R.id.geofences_list);

			// Create a custom adapter
			adapter = new ArrayAdapter<GeofenceData>(context, R.layout.list_geofences_element, android.R.id.text1, gfData) {
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					if (convertView == null) {
						convertView = getLayoutInflater().inflate(R.layout.list_geofences_element, parent, false);
					}

					TextView title = (TextView) convertView.findViewById(R.id.geofenceTitle);
					TextView address = (TextView) convertView.findViewById(R.id.geofenceAddress);
					ImageButton deleteButton = (ImageButton) convertView.findViewById(R.id.deleteGeofenceButton);

					title.setText(gfData.get(position).title);

					// If we don't have a known address and have Internet, update the address in the database
					ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
					if (cm.getActiveNetworkInfo() != null && (gfData.get(position).address.equals(getResources().getString(R.string.no_address_available)))) {
						Log.d(TAG, "Internet + no address. Trying to update address in the database..");
						// New objects
						String resolvedAddress = getResources().getString(R.string.no_address_available);
						GeofenceData currentGeofence = gfData.get(position);
						// Try to resolve address
						Geocoder gc = new Geocoder(context);
						try {
							List<Address> addresses = gc.getFromLocation(currentGeofence.lat, currentGeofence.lon, 1);
							resolvedAddress = addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getAddressLine(1);
						} catch (IOException e) {
						}

						// If we have a valid address
						if (resolvedAddress != getResources().getString(R.string.no_address_available)) {
							// Update in the database
							if (gfHelper.setAddressForGeofence(currentGeofence.id, resolvedAddress)) {
								gfData = gfHelper.getAllGeofences();
								// Notify that the dataset has changed
								adapter.notifyDataSetChanged();
							}
						}
					}

					address.setText(gfData.get(position).address + "\n" + String.format(getResources().getString(R.string.m_radius), (int) gfData.get(position).distance));

					deleteButton.setFocusable(false);
					deleteButton.setTag(R.id.TAG_ID, gfData.get(position).id);
					deleteButton.setTag(R.id.TAG_POSITION, position);

					return convertView;
				}
			};

			// Connect our custom adapter
			gfDataHolder.setAdapter(adapter);

			// Create a dialog for the user to pick a geofence
			geofencesList = new AlertDialog.Builder(context).setCancelable(true).setTitle(R.string.pick_geofence).setView(geofenceListLayout)
					.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					}).setNeutralButton(R.string.no_geofence, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							geofenceId = 0;
							handleAlarmPreview();
							dialog.dismiss();
						}
					}).setPositiveButton(R.string.create_new_geofence, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// GeofenceActivity intent
							Intent intent = new Intent(context, GeofenceActivity.class);
							intent.putExtra("listID", listID);
							intent.putExtra("title", listTitle);
							startActivityForResult(intent, GEOFENCE_REQUEST);
							overridePendingTransition(R.anim.right_in, R.anim.left_out);
						}
					}).create();

			// Show the dialog
			geofencesList.show();

			// When the user picks a geofence
			gfDataHolder.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Log.d(TAG, "Geofence selected!");
					GeofenceData geofence = adapter.getItem(position);
					geofenceId = geofence.id;
					handleAlarmPreview();
					geofencesList.dismiss();
				}
			});
		}
	}

	/**
	 * Deletes a geofence from the database
	 * 
	 * @param v The delete button that is clicked
	 * @since 1.0.0
	 */
	public void deleteGeofence(View v) {
		// Get the ID and the position of the geofence to delete
		int id = Integer.parseInt(v.getTag(R.id.TAG_ID).toString());
		int position = Integer.parseInt(v.getTag(R.id.TAG_POSITION).toString());
		// Delete from array
		gfData.remove(position);
		// Delete from database
		gfHelper.deleteGeofence(id);
		// Notify that the dataset has changed
		adapter.notifyDataSetChanged();

		// If this is the geofence that the user has currently selected
		if (id == geofenceId) {
			geofenceId = 0;
			handleAlarmPreview();
		}

		// If the user deleted all geofences
		if (gfData.size() == 0) {
			geofencesList.dismiss();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == GEOFENCE_REQUEST && resultCode == Activity.RESULT_OK) {
			Bundle extras = data.getExtras();
			Log.d(TAG, "geofenceId recieved: " + extras.getInt("geofenceId"));
			geofenceId = extras.getInt("geofenceId");
			handleAlarmPreview();
		}
	}

	@Override
	public void onBackPressed() {
		final Context context = this;
		int message;
		int action;

		// Set different messages, based on the context
		if (editMode) {
			action = R.string.discard_changes;
			message = R.string.confirm_discard_changes;
		} else {
			action = R.string.trash_photo;
			message = R.string.confirm_trash_photo;
		}

		AlertDialog confirmDeletion = new AlertDialog.Builder(this).setTitle(action).setMessage(message).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Create an intent
				Intent intent;

				// Which activity should be fired?
				if (!editMode) {
					intent = new Intent(context, CameraActivity.class);
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

}
