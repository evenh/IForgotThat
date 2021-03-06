package com.ehpefi.iforgotthat;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ehpefi.iforgotthat.swipelistview.BaseSwipeListViewListener;
import com.ehpefi.iforgotthat.swipelistview.SwipeListView;

/**
 * The activity view for a list containing list elements
 * 
 * @author Per Erik Finstad
 * @author Even Holthe
 * @since 1.0.0
 */
public class ListWithElementsActivity extends Activity {
	// UI Elements
	private TextView title;
	private TextView noReminders;
	private SwipeListView remindersView;

	// Incoming data
	private int listID = 0;
	private String listTitle = "N/A";

	// Helpers
	private ListHelper listHelper;
	private ListElementHelper listElementHelper;

	// Data
	private ArrayList<ListElementObject> elements;
	public ListElementObjectAdapter listAdapter;
	public int position;

	// Used for logging
	private static final String TAG = "ListWithElementsActivity";

	// Notification
	Toast toast;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Standard initialization
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_with_elements);

		// Find elements and assign elements
		noReminders = (TextView) findViewById(R.id.text_has_no_reminders);
		remindersView = (SwipeListView) findViewById(R.id.elementsListView);
		title = (TextView) findViewById(R.id.listName);

		// Initialize helpers
		listHelper = new ListHelper(this);
		listElementHelper = new ListElementHelper(this);

		// Get the intent and its content
		Bundle bundle = getIntent().getExtras();

		if (bundle != null) {
			listTitle = bundle.getString("title");
			listID = bundle.getInt("listID");
			if (listTitle == null || listTitle.equals("")) {
				try {
					listTitle = listHelper.getList(listID).getTitle();
				} catch (NullPointerException npe) {
					listTitle = getResources().getString(R.string.completed_items);
				}
			}
		} else {
			Log.e(TAG, "Fatal error: no data recieved!");
		}

		// Get TextView for the list title and set its name
		title.setText(listTitle);

		// Fill the elements
		elements = listElementHelper.getIncompleteListElementsForListId(listID, ListElementHelper.COL_ID);

		// Create a new list adapter for all our elements
		listAdapter = new ListElementObjectAdapter(this, R.layout.element_row, elements);
		remindersView.setAdapter(listAdapter);

		// Listener
		remindersView.setSwipeListViewListener(new BaseSwipeListViewListener() {
			// Whenever a row has been slided to show the back, update the list
			// position
			@Override
			public void onOpened(int pos, boolean toRight) {
				position = pos;
				Log.d(TAG, "Updated list position to " + pos);
			}

			// For clicks on the reminders
			@Override
			public void onClickFrontView(int position) {
				// Get the object for the clicked reminder
				ListElementObject reminder = (ListElementObject) remindersView.getItemAtPosition(position);

				// Create an intent and pass the object and list id
				Intent intent = new Intent(getApplicationContext(), DetailedReminderActivity.class);
				intent.putExtra("id", reminder.getId());
				intent.putExtra("listID", listID);

				startActivity(intent);

				// Transition smoothly :)
				overridePendingTransition(R.anim.right_in, R.anim.left_out);
			}
		});

		showElements();
	}

	/**
	 * Triggers a delete operation of the current reminder
	 * 
	 * @param button The view
	 * @since 1.0.0
	 */
	public void deleteElement(View button) {
		Log.d(TAG, "Deletion requested for list element in position: " + position);

		// Get the object to be deleted
		ListElementObject reminderObject = listAdapter.getItem(position);

		// Remove the list element from the array and delete the object from the
		// database
		elements.remove(position);
		listElementHelper.deleteListElement(reminderObject.getId());

		// Refresh view
		listAdapter.notifyDataSetChanged();

		// Close the slider
		remindersView.closeAnimate(position);

		// Run a check to show if we have an empty list
		showElements();
	}

	/**
	 * Triggers an edit operation of the current reminder
	 * 
	 * @param button The view
	 * @since 1.0.0
	 */
	public void editElement(View button) {
		Log.d(TAG, "Edit requested!");

		// Get object to be modified
		ListElementObject editObject = listAdapter.getItem(position);

		// Create a new intent
		Intent intent = new Intent(this, NewReminderActivity.class);
		intent.putExtra("editMode", true);
		intent.putExtra("id", editObject.getId());
		intent.putExtra("listID", editObject.getListId());

		// Fire off the new intent
		startActivity(intent);

		// Transition animation
		overridePendingTransition(R.anim.right_in, R.anim.left_out);
	}

	/**
	 * Triggers a completion operation of the current reminder
	 * 
	 * @param button The view
	 * @since 1.0.0
	 */
	public void completeElement(View button) {
		Log.d(TAG, "Completion requested for list element in position: " + position);

		// Get the object to be deleted
		ListElementObject reminderObject = listAdapter.getItem(position);

		// Remove the list element from the array and mark the reminder as
		// complete
		elements.remove(position);

		if (listID != ListElementHelper.COMPLETED_LIST_ID) {
			listElementHelper.setListElementComplete(reminderObject.getId(), true);
			displayToast(getResources().getString(R.string.reminder_completed));
		} else {
			// We are in the completed section
			listElementHelper.setListElementComplete(reminderObject.getId(), false);
			displayToast(getResources().getString(R.string.reminder_uncompleted));
		}

		// Refresh view
		listAdapter.notifyDataSetChanged();

		// Close the slider
		remindersView.closeAnimate(position);

		// Run a check to show if we have an empty list
		showElements();
	}

	/**
	 * Takes care of switching the views depending on the number of lists and if the user is in the "completed items" special list
	 * 
	 * @since 1.0.0
	 */
	private void showElements() {
		// If we are in the "complete" list, hide the new reminder button
		if (listID == ListElementHelper.COMPLETED_LIST_ID) {
			((GridLayout) findViewById(R.id.newListElementHolder)).setVisibility(View.GONE);
		}

		// If there are reminders
		if (elements.size() == 0) {
			// Hide the list view
			remindersView.setVisibility(View.GONE);
			noReminders.setVisibility(View.VISIBLE);

			return;
		}
		// Show the list view
		noReminders.setVisibility(View.GONE);
		remindersView.setVisibility(View.VISIBLE);
	}

	/**
	 * Goes back to the MainActivity
	 * 
	 * @since 1.0.0
	 */
	public void backToLists(View v) {
		// Calls the method that is called when the back button is pressed
		onBackPressed();
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivityForResult(intent, 0);
		// Transition animation
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

	/**
	 * Opens the CameraActivity class for creating
	 * 
	 * @param v The view
	 * @since 1.0.0
	 */
	public void newReminder(View v) {
		Intent intent = new Intent(getApplicationContext(), CameraActivity.class);

		intent.putExtra("listID", listID);
		startActivity(intent);

		// Transition smoothly :)
		overridePendingTransition(R.anim.right_in, R.anim.left_out);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
