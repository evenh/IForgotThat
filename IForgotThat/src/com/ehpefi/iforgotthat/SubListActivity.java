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
		
		// title = (TextView) findViewById(R.id.listName);
		// title.setText("It's works!");

		Intent intent = getIntent();
		
		String listName = intent.getStringExtra(("name"));

		title = (TextView) findViewById(R.id.listName);

		title.setText(listName);

	}

	// calls mainactivity on clicked button
	public void backToMainList(View v) {
		Intent intent = new Intent(this, MainActivity.class);
		startActivityForResult(intent, 0);
	}

}
