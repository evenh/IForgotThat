package com.ehpefi.iforgotthat;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

/**
 * The main activity which displays all the available lists
 * 
 * @author Per Erik Finstad
 * @author Even Holthe
 * @since 1.0
 */
public class MainActivity extends Activity {
	// UI Elements
	ListView listView;
	EditText listNameInput;
	TextView hasNoLists;

	ViewSwitcher switcher;

	// Database related
	ListHelper listHelper = new ListHelper(this);
	ListElementHelper listElementHelper = new ListElementHelper(this);
	ArrayList<ListObject> allLists;
	ListObjectAdapter listAdapter;

	// Used for logging
	private static final String TAG = "MainActivity";

	// Notification
	Toast toast;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Find our UI elements
		listView = (ListView) findViewById(R.id.mainView);
		listNameInput = (EditText) findViewById(R.id.editable_add_list_input);
		hasNoLists = (TextView) findViewById(R.id.text_has_no_lists);

		switcher = (ViewSwitcher) findViewById(R.id.add_list_switcher);

		// Fetch all the lists from the database
		allLists = listHelper.getAllLists(ListHelper.COL_ID);
		// Create a new list adapter for all our lists
		listAdapter = new ListObjectAdapter(this, android.R.layout.simple_list_item_1, allLists);
		listView.setAdapter(listAdapter);

		showLists();
	}

	/**
	 * Takes care of showing an empty message if there are no lists, and all the lists if number of lists > 0
	 * 
	 * @since 1.0
	 */
	private void showLists() {
		// If the database doesn't contain any lists
		if (listHelper.numberOfLists() == 0) {

			// Hide the list view
			listView.setVisibility(View.GONE);
			hasNoLists.setVisibility(View.VISIBLE);

			Log.d(TAG, "No lists in the database");

			return;
		}

		// Show the list view if we have data
		hasNoLists.setVisibility(View.GONE);
		listView.setVisibility(View.VISIBLE);

		// When a list is clicked
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				// Get the object for the clicked list
				ListObject list = (ListObject) listView.getItemAtPosition(position);

				// Create an intent and pass the title and id
				Intent intent = new Intent(getApplicationContext(), ListWithElementsActivity.class);
				intent.putExtra("title", list.getTitle());
				intent.putExtra("id", list.getId());

				startActivity(intent);

				// Transition smoothly :)
				overridePendingTransition(R.anim.right_in, R.anim.left_out);
			}
		});
	}

	/**
	 * Requests the change from TextView to EditText
	 * 
	 * @param view The view
	 * @since 1.0
	 */
	public void addNewListClicked(View view) {
		switcher.showNext();
	}

	/**
	 * Convenience method for displaying a toast message
	 * 
	 * @param message The message to display
	 * @since 1.0
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
	 * Convenience method for refreshing the list view
	 * 
	 * @since 1.0
	 */
	private void updateListView() {
		allLists = listHelper.getAllLists(ListHelper.COL_ID);
		listAdapter.clear();
		listAdapter.addAll(allLists);
		showLists();
	}

	/**
	 * Handle the user input after they have provided a list name
	 * 
	 * @param view The view
	 * @since 1.0
	 */
	public void addNewListTextEntered(View view) {
		// The list name the user typed in
		String inputName = listNameInput.getText().toString();
		Log.d(TAG, "The list name that was entered was: " + inputName);

		// Check for empty or invalid input
		if (inputName.equals("") || inputName == null || inputName.trim().length() == 0) {
			displayToast(getResources().getString(R.string.list_name_empty));
			return;
		}

		// Check for a list named the same as the input
		if (listHelper.doesListWithTitleExist(inputName)) {
			displayToast(String.format(getResources().getString(R.string.list_already_exists), inputName));
		} else {
			// Try to create the list
			if (listHelper.createNewList(inputName) > 0) {
				// All good, update the data and tell the listAdapter to refresh
				updateListView();
				// Tell the user that everything went OK
				displayToast(String.format(getResources().getString(R.string.list_creation_ok), inputName));
				// Close the keyboard
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(listNameInput.getWindowToken(), 0);
				// Show the add new list text
				switcher.showNext();
				// Reset the text in the EditText
				listNameInput.setText("");
			} else {
				// Just display a toast telling the user that the creation of a new list failed
				displayToast(String.format(getResources().getString(R.string.list_creation_fail), inputName));
			}
		}

	}

}
