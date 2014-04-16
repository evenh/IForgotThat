package com.ehpefi.iforgotthat;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ehpefi.iforgotthat.swipelistview.SwipeListView;

/**
 * The activity view for a list containing list elements
 * 
 * @author Per Erik Finstad
 * @since 1.0
 */
public class ListWithElementsActivity extends Activity {
	private TextView title;
	private TextView noReminders;
	private SwipeListView remindersView;

	private int listID = 0;
	private String listTitle = "N/A";

	private ListHelper listHelper;
	private ListElementHelper listElementHelper;

	private ArrayList<ListElementObject> elements;
	ListElementObjectAdapter listAdapter;

	BroadcastReceiver postman;
	
	// Used for logging
	private static final String TAG = "ListWithElementsActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_with_elements);

		// Find elements
		noReminders = (TextView) findViewById(R.id.text_has_no_reminders);
		remindersView = (SwipeListView) findViewById(R.id.elementsListView);

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
		title = (TextView) findViewById(R.id.listName);
		title.setText(listTitle);

		// Fill the elements
		elements = listElementHelper.getIncompleteListElementsForListId(listID, ListElementHelper.COL_ID);

		// Create a new list adapter for all our elements
		listAdapter = new ListElementObjectAdapter(this, R.layout.element_row, elements, listID);
		remindersView.setAdapter(listAdapter);

		showElements();

		// Recieves messages to update the list
		postman = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Bundle extras = intent.getExtras();

				elements.remove(extras.getInt("position"));
				Log.d(TAG, "Size of 'elements' is " + elements.size());


				Log.d(TAG, "Incoming position to be closed: " + extras.getInt("position"));

				remindersView.closeAnimate(extras.getInt("position"));
				elements = listElementHelper.getIncompleteListElementsForListId(listID, ListElementHelper.COL_ID);
				listAdapter.notifyDataSetChanged();

				Log.d(TAG, "Size of 'elements' is NOW " + elements.size());

				updateListView();
			}
		};

		LocalBroadcastManager.getInstance(this).registerReceiver(postman, new IntentFilter("update-list"));
	}

	/**
	 * Convenience method for refreshing the list view
	 * 
	 * @since 1.0
	 */
	public void updateListView() {
		Log.d(TAG, "Updating the list view...");
		elements = null;
		elements = listElementHelper.getIncompleteListElementsForListId(listID, ListElementHelper.COL_ID);
		listAdapter.clear();
		listAdapter.addAll(elements);
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
		Intent intent = new Intent(getApplicationContext(), CameraActivity.class);

		intent.putExtra("listID", listID);
		startActivity(intent);

		// Transition smoothly :)
		overridePendingTransition(R.anim.right_in, R.anim.left_out);
	}

	@Override
	protected void onDestroy() {
		// Unregister since the activity is about to be closed.
		LocalBroadcastManager.getInstance(this).unregisterReceiver(postman);
		super.onDestroy();
	}
}
