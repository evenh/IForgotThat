package com.ehpefi.iforgotthat;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Custom adapter for just showing the title of the list
 * 
 * @author Even Holthe
 * @since 1.0
 */
public class ListObjectAdapter extends ArrayAdapter<ListObject> {
	private ArrayList<ListObject> lists;

	public ListObjectAdapter(Context context, int textViewResourceId, ArrayList<ListObject> lists) {
		super(context, textViewResourceId, lists);
		this.lists = lists;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Convert the View passed in to a local variable
		View view = convertView;

		if (view == null) {
			// Create a new inflater (basically rendering/showing an XML layout file)
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// For now, use the standard Android simple list item 1
			view = inflater.inflate(android.R.layout.simple_list_item_1, null);
		}

		// Get the current object
		ListObject listObject = lists.get(position);

		if (listObject != null) {
			// Find the textview to hold the title
			TextView title = (TextView) view.findViewById(android.R.id.text1);
			// Set the title for the current list
			title.setText(listObject.getTitle());
		}

		return view;
	}
}
