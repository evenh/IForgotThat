package com.ehpefi.iforgotthat;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Custom adapter for just showing the title of the list
 * 
 * @author Even Holthe
 * @since 1.0.0
 */
public class ListObjectAdapter extends ArrayAdapter<ListObject> {
	// Data and helper
	private ArrayList<ListObject> lists;
	private ListHelper listHelper;

	public ListObjectAdapter(Context context, int textViewResourceId, ArrayList<ListObject> lists) {
		super(context, textViewResourceId, lists);
		this.lists = lists;
		listHelper = new ListHelper(context);
	}

	@Override
	public int getCount() {
		return lists.size();
	}

	@Override
	public ListObject getItem(int pos) {
		return lists.get(pos);
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
	class ListRow {
		// Front
		TextView listName;

		// Back
		ImageButton deleteButton;
		ImageButton renameButton;

		public ListRow(View row) {
			listName = (TextView) row.findViewById(R.id.listName);

			deleteButton = (ImageButton) row.findViewById(R.id.btn_delete_list);
			renameButton = (ImageButton) row.findViewById(R.id.btn_rename_list);
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// A helper class to show our data
		ListRow rowHolder;

		// Convert the View passed in to a local variable
		View view = convertView;

		if (view == null) {
			// Create a new inflater (basically rendering/showing an XML layout file)
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// For now, use the standard Android simple list item 1
			view = inflater.inflate(R.layout.list_row, null);

			rowHolder = new ListRow(view);
			view.setTag(rowHolder);
		} else {
			// If view != null
			rowHolder = (ListRow) view.getTag();
		}

		// Get the current object
		ListObject listObject = getItem(position);

		if (listObject != null) {
			// Set the title for the current list
			rowHolder.listName.setText(listObject.getTitle());
		}

		return view;
	}
}
