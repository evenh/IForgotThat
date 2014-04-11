package com.ehpefi.iforgotthat;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewSwitcher;

/**
 * The activity view for a list containing list elements
 * 
 * @author Per Erik Finstad
 * @since 1.0
 */
public class ListWithElementsActivity extends Activity {
	TextView title;
	ViewSwitcher viewSwitcher;

	private int listID = 0;
	private String listTitle = "N/A";

	ListHelper listHelper;
	ListElementHelper listElementHelper;

	ArrayList<ListElementObject> elements;

	// Used for logging
	private static final String TAG = "ListWithElementsActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_with_elements);
		
		listHelper = new ListHelper(this);
		listElementHelper = new ListElementHelper(this);
		
		viewSwitcher = (ViewSwitcher) findViewById(R.id.viewswitch);

		// Get the intent and its content
		Bundle bundle = getIntent().getExtras();

		if (bundle != null) {
			listTitle = bundle.getString("title");
			listID = bundle.getInt("listID");
			if (listTitle == null || listTitle.equals("")) {
				listTitle =  listHelper.getList(listID).getTitle();
			}
		}

		// Get TextView for the list title and set its name
		title = (TextView) findViewById(R.id.listName);
		title.setText(listTitle);

		// Fill the elements
		elements = listElementHelper.getListElementsForListId(listID, ListElementHelper.COL_ID);
		
		showElements();
	}

	private void showElements() {
		// If there are reminders
		if (elements.size() > 0) {
			Log.d(TAG, "Number of reminders: " + elements.size());
			viewSwitcher.showNext();
		}
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

}
