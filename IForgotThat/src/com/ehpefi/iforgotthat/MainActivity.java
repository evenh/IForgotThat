package com.ehpefi.iforgotthat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ViewSwitcher;

/**
 * The main activity which displays all the available lists
 * 
 * @author Per Erik Finstad
 * @since 1.0
 */
public class MainActivity extends Activity {
	// UI Elements
	ListView listView;
	EditText listNameInput;

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

		showLists();
	}

	private void showLists() {
		// If the database doesn't contain any lists
		if (listHelper.numberOfLists() == 0) {

			Log.d(TAG, "No lists in the database");

			return;
		}

		// for development only. the array will get its data from the database in prod
		String[] values = new String[] { "Important", "Weekend reminders", "Today", "Before I go to bed",
				"Before next week" };

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
				android.R.id.text1, values);

		// assign adapter to listView
		listView.setAdapter(adapter);

		// set clicklistener
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				// get clicked item value
				String itemValue = (String) listView.getItemAtPosition(position);

				Intent intent = new Intent(getApplicationContext(), ListWithElementsActivity.class);
				intent.putExtra("name", itemValue);// add the item name to the intent

				startActivity(intent);

				// transition
				overridePendingTransition(R.anim.right_in, R.anim.left_out);
			}
		});// end of clicklistener
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

}// end of activity

