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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_with_elements);
		
		// Get the intent and its content
		Intent intent = getIntent();
		String listTitle = intent.getStringExtra(("title"));

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

}
