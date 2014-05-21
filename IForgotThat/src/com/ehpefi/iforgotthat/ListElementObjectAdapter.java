package com.ehpefi.iforgotthat;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
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
	private final ArrayList<ListElementObject> elements;
	private final ListElementHelper listElementHelper;
	private ListHelper listHelper;
	private final GeofenceHelper gfHelper;

	// Context and data
	public Context context;
	public ListElementObject reminder;

	// For debugging
	public static final String TAG = "ListElementObjectAdapter";

	public ListElementObjectAdapter(Context context, int textViewResourceId, ArrayList<ListElementObject> elements) {
		super(context, textViewResourceId, elements);
		this.elements = elements;
		this.context = context;

		// Initialize new helper instances
		listElementHelper = new ListElementHelper(this.context);
		gfHelper = new GeofenceHelper(this.context);
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
		TextView belongingList;

		// Back
		ImageButton thrashButton;
		ImageButton editButton;
		ImageButton completeButton;

		public ElementRow(View row) {
			image = (ImageView) row.findViewById(R.id.reminderImage);
			description = (TextView) row.findViewById(R.id.lblDescription);
			alarm = (TextView) row.findViewById(R.id.lblAlarm);
			belongingList = (TextView) row.findViewById(R.id.lblListName);
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

		// Get the current object
		reminder = getItem(position);

		// Hide the list name
		rowHolder.belongingList.setVisibility(View.GONE);

		if (reminder != null) {
			Bitmap reminderImage = reminder.getImageAsBitmap();
			rowHolder.image.setImageBitmap(reminderImage);

			// If we have an alarm
			if (!reminder.getAlarmAsString().equals(ListElementObject.noAlarmString)) {
				rowHolder.alarm.setText(reminder.getAlarmAsString());
			}

			// If we have a geofence alarm
			if (reminder.getGeofenceId() > 0) {
				rowHolder.alarm.setText((gfHelper.getGeofence(reminder.getGeofenceId()).title));
				// rowHolder.alarm.setText(gfHelper.getAddressForGeofence(reminder.getGeofenceId()));
			}

			// If we have a description
			if (!reminder.getDescription().equals("")) {
				rowHolder.description.setText(reminder.getDescription());
			}

			// If the item is marked as complete
			if (reminder.isCompleted()) {
				// Hide the 'edit' button
				rowHolder.editButton.setVisibility(View.GONE);
				// Show the list name
				listHelper = new ListHelper(context);
				ListObject list = listHelper.getList(reminder.getListId());
				rowHolder.belongingList.setText(String.format(context.getResources().getString(R.string.list_belonging), list.getTitle()));
				rowHolder.belongingList.setVisibility(View.VISIBLE);
			}
		}

		reminder = null;
		return view;
	}

	public void clearData() {
		elements.clear();
	}
}
