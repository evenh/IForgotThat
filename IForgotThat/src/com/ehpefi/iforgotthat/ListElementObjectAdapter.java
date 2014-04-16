package com.ehpefi.iforgotthat;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
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
 * @since 1.0
 */
public class ListElementObjectAdapter extends ArrayAdapter<ListElementObject> {
	private ArrayList<ListElementObject> elements;
	private ListElementHelper listElementHelper;

	Context context;

	public static final String TAG = "ListElementObjectAdapter";

	public ListElementObjectAdapter(Context context, int textViewResourceId, ArrayList<ListElementObject> elements) {
		super(context, textViewResourceId, elements);
		this.elements = elements;
		this.context = context;
	}

	private void sendMessageToUpdateListView(int position) {
		Intent intent = new Intent("update-list");
		// Which list element to get closed
		intent.putExtra("position", position);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// Convert the View passed in to a local variable
		View view = convertView;

		if (view == null) {
			// Create a new inflater (basically rendering/showing an XML layout file)
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// For now, use the standard Android simple list item 1
			view = inflater.inflate(R.layout.element_row, null);
		}

		listElementHelper = new ListElementHelper(context);

		// Get the current object
		final ListElementObject reminder = elements.get(position);

		ImageView image = (ImageView) view.findViewById(R.id.reminderImage);
		TextView description = (TextView) view.findViewById(R.id.lblDescription);
		TextView alarm = (TextView) view.findViewById(R.id.lblAlarm);

		// Our "hidden" buttons
		ImageButton thrashButton = (ImageButton) view.findViewById(R.id.btn_trash_reminder);
		ImageButton editButton = (ImageButton) view.findViewById(R.id.btn_edit_reminder);
		ImageButton completeButton = (ImageButton) view.findViewById(R.id.btn_complete_reminder);

		if (reminder != null) {
			byte[] tmpImg = reminder.getImage();
			Bitmap bmp = BitmapFactory.decodeByteArray(tmpImg, 0, tmpImg.length);
			image.setImageBitmap(bmp);

			// If we have an alarm
			if (!reminder.getAlarmAsString().equals(ListElementObject.noAlarmString)) {
				alarm.setText(reminder.getAlarmAsString());
			}

			// If we have a description
			if (!reminder.getDescription().equals("")) {
				description.setText(reminder.getDescription());
			}

			// If the user clicked "thrash"
			thrashButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.d(TAG, "Deletion requested!");
					listElementHelper.deleteListElement(reminder.getId());
					sendMessageToUpdateListView(position);
				}
			});

			// If the user clicked "edit"
			editButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.d(TAG, "Editing requested!");
				}
			});

			// If the user clicked "complete"
			completeButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.d(TAG, "Completion requested!");
				}
			});
		}

		return view;
	}
}
