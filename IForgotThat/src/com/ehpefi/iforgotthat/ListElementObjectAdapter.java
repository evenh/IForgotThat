package com.ehpefi.iforgotthat;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

/**
 * Custom adapter for just showing the title of the list
 * 
 * @author Even Holthe
 * @since 1.0
 */
public class ListElementObjectAdapter extends ArrayAdapter<ListElementObject> {

	private ArrayList<ListElementObject> elements;

	public ListElementObjectAdapter(Context context, int textViewResourceId, ArrayList<ListElementObject> elements) {
		super(context, textViewResourceId, elements);
		this.elements = elements;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Convert the View passed in to a local variable
		View view = convertView;

		if (view == null) {
			// Create a new inflater (basically rendering/showing an XML layout file)
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// For now, use the standard Android simple list item 1
			view = inflater.inflate(R.layout.element_row, null);
		}

		// Get the current object
		ListElementObject reminder = elements.get(position);
		ImageView image = (ImageView) view.findViewById(R.id.reminderImage);

		if (reminder != null) {
			byte[] tmpImg = reminder.getImage();
			Bitmap bmp = BitmapFactory.decodeByteArray(tmpImg, 0, tmpImg.length);
			image.setImageBitmap(bmp);
		}
		/*
		 * if (reminder != null) { // Find the ImageView ImageView image = (ImageView)
		 * view.findViewById(R.id.reminderImage);
		 * image.setImageBitmap(BitmapFactory.decodeByteArray(reminder.getImage(), 0, reminder.getImage().length)); }
		 */

		return view;
	}
}
