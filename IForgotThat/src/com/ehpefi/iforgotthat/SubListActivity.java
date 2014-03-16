package com.ehpefi.iforgotthat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SubListActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sub_list);
	}

	public void backToMainList(View v) {
		Intent intent = new Intent(this, MainActivity.class);

		startActivityForResult(intent, 0);
	}

}
