package com.ehpefi.iforgotthat;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ehpefi.iforgotthat.swipelistview.BaseSwipeListViewListener;
import com.ehpefi.iforgotthat.swipelistview.SwipeListView;

/**
 * The activity view for a list containing list elements
 * 
 * @author Per Erik Finstad
 * @since 1.0
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
				listTitle = listHelper.getList(listID).getTitle();
			}
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
			// Whenever a row has been slided to show the back, update the list position
			public void onOpened(int pos, boolean toRight) {
				position = pos;
				Log.d(TAG, "Updated list position to " + pos);
			}
		});

		showElements();
	}

	public void deleteElement(View button) {
		Log.d(TAG, "Deletion requested for list element in position: " + position);

		// Get the object to be deleted
		ListElementObject reminderObject = listAdapter.getItem(position);

		// Close the slider
		remindersView.closeAnimate(position);

		// Remove the list element from the array and delete the object from the database
		elements.remove(position);
		listElementHelper.deleteListElement(reminderObject.getId());

		// Refresh view
		listAdapter.notifyDataSetChanged();

		// Run a check to show if we have an empty list
		showElements();
	}

	public void editElement(View button) {
		Log.d(TAG, "Edit requested!");
	}

	public void completeElement(View button) {
		Log.d(TAG, "Completion requested for list element in position: " + position);

		// Get the object to be deleted
		ListElementObject reminderObject = listAdapter.getItem(position);

		// Close the slider
		remindersView.closeAnimate(position);

		// Remove the list element from the array and mark the reminder as complete
		elements.remove(position);

		if (listID != ListElementHelper.COMPLETED_LIST_ID) {
			listElementHelper.setListElementComplete(reminderObject.getId(), true);
		} else {
			// We are in the completed section
			listElementHelper.setListElementComplete(reminderObject.getId(), false);
		}

		// Refresh view
		listAdapter.notifyDataSetChanged();

		// Run a check to show if we have an empty list
		showElements();
	}

	private void showElements() {
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
	 * @since 1.0
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
	 * Opens the CameraActivity class for creating
	 * 
	 * @param v The view
	 * @since 1.0
	 */
	public void newReminder(View v) {
		Intent intent = new Intent(getApplicationContext(), CamTest.class);

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
