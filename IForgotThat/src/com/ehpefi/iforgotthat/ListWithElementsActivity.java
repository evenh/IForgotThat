package com.ehpefi.iforgotthat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * The activity view for a list containing list elements
 * 
 * @author Per Erik Finstad
 * @since 1.0
 */
public class ListWithElementsActivity extends Activity {
	TextView title;
	private int listID = 0;
	ListHelper listHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_with_elements);
		
		listHelper = new ListHelper(this);
		
		// Get the intent and its content
		Bundle bundle = getIntent().getExtras();
		String listTitle = "N/A";

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
