package com.ehpefi.iforgotthat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity {
	// public final static String LIST_NAME = com.ehpefi.iforgotthat.MESSAGE;
	ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		listView = (ListView) findViewById(R.id.mainView);
		
		// for development only. the array will get its data from the database
		// in prod
		String[] values = new String[] { "Important", "Weekend reminders",
				"Today", "Before I go to bed" };

		// Define a new Adapter
		// First parameter - Context
		// Second parameter - Layout for the row
		// Third parameter - ID of the TextView to which the data is written
		// Forth - the Array of data
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1, values);


		// assign adapter to listView
		listView.setAdapter(adapter);
		
		//set clicklsitener
		listView.setOnItemClickListener(new OnItemClickListener(){
			

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long id) {

				// call sublist activty
				
				Intent intent = new Intent(null, SubListActivity.class);

				startActivityForResult(intent, 0);


			}
		});
	}

}// end of activity

