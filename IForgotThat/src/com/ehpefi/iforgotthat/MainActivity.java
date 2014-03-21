package com.ehpefi.iforgotthat;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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

	// Database related
	ListHelper listHelper = new ListHelper(this);
	ListElementHelper listElementHelper = new ListElementHelper(this);

	// Used for logging
	private static final String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Find our UI elements
		listView = (ListView) findViewById(R.id.mainView);
		listNameInput = (EditText) findViewById(R.id.editable_add_list_input);
		hasNoLists = (TextView) findViewById(R.id.text_has_no_lists);

		showLists();
	}

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

		// Fetch all the lists from the database
		ArrayList<ListObject> allLists = listHelper.getAllLists(listHelper.COL_ID);

		// Create a new list adapter for all our lists
		ListObjectAdapter listAdapter = new ListObjectAdapter(this, android.R.layout.simple_list_item_1,
				allLists);

		listView.setAdapter(listAdapter);

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
		ViewSwitcher switcher = (ViewSwitcher) findViewById(R.id.add_list_switcher);
		switcher.showNext();
	}

	public void addNewListTextEntered(View view) {
		// TODO: Make this a method not used just for debugging purposes
		Log.d(TAG, "The list name that was entered was: " + listNameInput.getText());
	}

}
