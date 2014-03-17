package com.ehpefi.iforgotthat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class SubListActivity extends Activity {
	TextView title;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sub_list);
		
		// get the intent and its content
		Intent intent = getIntent();
		String listName = intent.getStringExtra(("name"));

		// get textview for the list title and set its name
		title = (TextView) findViewById(R.id.listName);
		title.setText(listName);

	}

	// calls mainactivity on clicked button
	public void backToMainList(View v) {
		Intent intent = new Intent(this, MainActivity.class);
		startActivityForResult(intent, 0);
		// transition animation
		overridePendingTransition(R.anim.left_in, R.anim.right_out);
	}

}
