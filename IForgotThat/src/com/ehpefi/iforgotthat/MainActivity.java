package com.ehpefi.iforgotthat;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class MainActivity extends Activity {
	TableLayout lv_lists;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		lv_lists = (TableLayout) findViewById(R.id.lv_lists_sv_table);
		fillListsTable();
	}

	void fillListsTable() {
		TableRow row;
		TextView t1;

		for (int i = 1; i <= 50; i++) {
			row = new TableRow(this);
			// row.setBackgroundColor(Color.parseColor("#333333"));

			t1 = new TextView(this);
			t1.setTextColor(Color.parseColor("#000000"));
			t1.setText("List #" + i);
			t1.setTypeface(Typeface.MONOSPACE);
			t1.setTextSize(30);

			row.addView(t1);

			lv_lists.addView(row, new TableLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		}

		row = new TableRow(this);
		t1 = new TextView(this);
		t1.setText(getResources().getString(R.string.completed_items) + " →");
		t1.setTypeface(Typeface.DEFAULT_BOLD);
		t1.setTextSize(30);
		row.addView(t1);
		lv_lists.addView(row, new TableLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}
}
