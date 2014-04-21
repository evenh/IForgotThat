package com.ehpefi.iforgotthat;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Custom adapter for just showing the title of the list
 * 
 * @author Even Holthe
 * @since 1.0.0
 */
public class ListElementObjectAdapter extends ArrayAdapter<ListElementObject> {
	// Our data
	private ArrayList<ListElementObject> elements;
	private ListElementHelper listElementHelper;

	// Context and data
	public Context context;
	public ListElementObject reminder;

	// For debugging
	public static final String TAG = "ListElementObjectAdapter";

	public ListElementObjectAdapter(Context context, int textViewResourceId, ArrayList<ListElementObject> elements) {
		super(context, textViewResourceId, elements);
		this.elements = elements;
		this.context = context;
	}

	@Override
	public int getCount() {
		return elements.size();
	}

	@Override
	public ListElementObject getItem(int pos) {
		return elements.get(pos);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * A class to hold one row (one reminder)
	 * 
	 * @author Even Holthe
	 * @since 1.0.0
	 */
	class ElementRow {
		// Front
		ImageView image;
		TextView description;
		TextView alarm;

		// Back
		ImageButton thrashButton;
		ImageButton editButton;
		ImageButton completeButton;

		public ElementRow(View row) {
			image = (ImageView) row.findViewById(R.id.reminderImage);
			description = (TextView) row.findViewById(R.id.lblDescription);
			alarm = (TextView) row.findViewById(R.id.lblAlarm);
			thrashButton = (ImageButton) row.findViewById(R.id.btn_trash_reminder);
			editButton = (ImageButton) row.findViewById(R.id.btn_edit_reminder);
			completeButton = (ImageButton) row.findViewById(R.id.btn_complete_reminder);
		}
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// A helper class to show our data
		ElementRow rowHolder;

		// Convert the View passed in to a local variable
		View view = convertView;

		if (view == null) {
			// Create a new inflater (basically rendering/showing an XML layout file)
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// For now, use the standard Android simple list item 1
			view = inflater.inflate(R.layout.element_row, null);

			rowHolder = new ElementRow(view);
			view.setTag(rowHolder);
		} else {
			// If view != null
			rowHolder = (ElementRow) view.getTag();
		}

		// Initialize a new helper instance
		listElementHelper = new ListElementHelper(getContext());

		// Get the current object
		reminder = getItem(position);

		if (reminder != null) {
			byte[] tmpImg = reminder.getImage();
			Bitmap bmp = BitmapFactory.decodeByteArray(tmpImg, 0, tmpImg.length);
			rowHolder.image.setImageBitmap(bmp);

			// If we have an alarm
			if (!reminder.getAlarmAsString().equals(ListElementObject.noAlarmString)) {
				rowHolder.alarm.setText(reminder.getAlarmAsString());
			}

			// If we have a description
			if (!reminder.getDescription().equals("")) {
				rowHolder.description.setText(reminder.getDescription());
			}
		}

		return view;
	}
}
