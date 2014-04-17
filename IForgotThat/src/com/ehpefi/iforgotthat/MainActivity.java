package com.ehpefi.iforgotthat;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

/**
 * The main activity which displays all the available lists
 * 
 * @author Per Erik Finstad
 * @author Even Holthe
 * @since 1.0
 */
public class MainActivity extends Activity {
	// UI Elements
	ListView listView;
	EditText listNameInput;
	TextView hasNoLists;

	ViewSwitcher switcher;

	// Database related
	ListHelper listHelper = new ListHelper(this);
	ListElementHelper listElementHelper = new ListElementHelper(this);
	ArrayList<ListObject> allLists;
	ListObjectAdapter listAdapter;

	// Used for logging
	private static final String TAG = "MainActivity";

	// Notification
	Toast toast;

	// Context of this class
	final Context context = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Find our UI elements
		listView = (ListView) findViewById(R.id.mainView);
		listNameInput = (EditText) findViewById(R.id.editable_add_list_input);
		hasNoLists = (TextView) findViewById(R.id.text_has_no_lists);

		switcher = (ViewSwitcher) findViewById(R.id.add_list_switcher);

		// Fetch all the lists from the database
		allLists = listHelper.getAllLists(ListHelper.COL_ID);
		// Create a new list adapter for all our lists
		listAdapter = new ListObjectAdapter(this, android.R.layout.simple_list_item_1, allLists);
		listView.setAdapter(listAdapter);
		registerForContextMenu(listView);

		showLists();
	}

	/**
	 * Takes care of showing an empty message if there are no lists, and all the lists if number of lists > 0
	 * 
	 * @since 1.0
	 */
	private void showLists() {
		// If the database doesn't contain any lists
		if (listHelper.numberOfLists() == 0) {

			// Hide the list view
			listView.setVisibility(View.GONE);
			hasNoLists.setVisibility(View.VISIBLE);

			Log.d(TAG, "No lists in the database");

			return;
		}

		// Show the list view if we have data
		hasNoLists.setVisibility(View.GONE);
		listView.setVisibility(View.VISIBLE);

		// When a list is clicked
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				// Get the object for the clicked list
				ListObject list = (ListObject) listView.getItemAtPosition(position);

				// Create an intent and pass the title and id
				Intent intent = new Intent(getApplicationContext(), ListWithElementsActivity.class);
				intent.putExtra("title", list.getTitle());
				intent.putExtra("listID", list.getId());

				startActivity(intent);

				// Transition smoothly :)
				overridePendingTransition(R.anim.right_in, R.anim.left_out);
			}
		});
	}

	/**
	 * Shows the completed items
	 * 
	 * @param view The view
	 * @since 1.0
	 */
	public void showCompletedItems(View view) {
		Intent completed = new Intent(this, ListWithElementsActivity.class);
		completed.putExtra("title", getResources().getString(R.string.completed_items));
		completed.putExtra("listID", ListElementHelper.COMPLETED_LIST_ID);

		startActivity(completed);

		// Transition smoothly :)
		overridePendingTransition(R.anim.right_in, R.anim.left_out);
	}

	/**
	 * Requests the change from TextView to EditText
	 * 
	 * @param view The view
	 * @since 1.0
	 */
	public void addNewListClicked(View view) {
		switcher.showNext();
	}

	/**
	 * Convenience method for displaying a toast message
	 * 
	 * @param message The message to display
	 * @since 1.0
	 */
	private void displayToast(String message) {
		// If there is an active toast, cancel it
		if (toast != null) {
			toast.cancel();
		}

		// Create a toast
		toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
		// Set the position to right above the keyboard (on Nexus S at least)
		toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, -50);
		toast.show();
	}

	/**
	 * Convenience method for refreshing the list view
	 * 
	 * @since 1.0
	 */
	private void updateListView() {
		allLists = listHelper.getAllLists(ListHelper.COL_ID);
		listAdapter.clear();
		listAdapter.addAll(allLists);
		showLists();
	}

	/**
	 * Handle the user input after they have provided a list name
	 * 
	 * @param view The view
	 * @since 1.0
	 */
	public void addNewListTextEntered(View view) {
		// The list name the user typed in
		String inputName = listNameInput.getText().toString();
		Log.d(TAG, "The list name that was entered was: " + inputName);

		if (inputName.equals("") || inputName == null || inputName.trim().length() == 0) {
			// Send toast to user
			displayToast(getResources().getString(R.string.list_name_empty));

			// Dismiss keyboard and switch back to text saying "Add new list"
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(listNameInput.getWindowToken(), 0);
			switcher.showNext();

			return;
		}

		// Check for a list named the same as the input
		if (listHelper.doesListWithTitleExist(inputName)) {
			displayToast(String.format(getResources().getString(R.string.list_already_exists), inputName));
		} else {
			// Try to create the list
			if (listHelper.createNewList(inputName) > 0) {
				// All good, update the data and tell the listAdapter to refresh
				updateListView();
				// Tell the user that everything went OK
				displayToast(String.format(getResources().getString(R.string.list_creation_ok), inputName));
				// Close the keyboard
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(listNameInput.getWindowToken(), 0);
				// Show the add new list text
				switcher.showNext();
				// Reset the text in the EditText
				listNameInput.setText("");
			} else {
				// Just display a toast telling the user that the creation of a new list failed
				displayToast(String.format(getResources().getString(R.string.list_creation_fail), inputName));
			}
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		// Check whether the ListView was selected
		if (view.getId() == R.id.mainView) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			// Set list title
			menu.setHeaderTitle(String.format(getResources().getString(R.string.list_menu_title),
					allLists.get(info.position).getTitle()));
			// Get available actions
			String[] menuItems = getResources().getStringArray(R.array.list_modifiers);
			for (int i = 0; i < menuItems.length; i++) {
				menu.add(Menu.NONE, i, i, menuItems[i]);
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

		String[] menuItems = getResources().getStringArray(R.array.list_modifiers);
		int index = item.getItemId();
		String itemName = menuItems[index];
		final ListObject selectedList = allLists.get(info.position);

		// If the user selected "Delete"
		if (itemName.equals(getResources().getString(R.string.list_menu_delete))) {
			// Create a dialog box to confirm deletion
			AlertDialog confirmDeletion = new AlertDialog.Builder(this)
					.setTitle(getResources().getString(R.string.list_deletion_title))
					.setMessage(String.format(getResources().getString(R.string.list_deletion_confirm), selectedList.getTitle()))
					.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// If we successfully deleted the list
							if (listHelper.deleteList(selectedList.getId(), context)) {
								displayToast(String.format(getResources().getString(R.string.list_deletion_ok), selectedList.getTitle()));
							} else {
								// Couldn't delete the list
								displayToast(String.format(getResources().getString(R.string.list_deletion_fail), selectedList.getTitle()));
							}

							updateListView();
						}
					}).setNegativeButton(getResources().getString(R.string.no), new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					}).create();

			confirmDeletion.show();
		} else if (itemName.equals(getResources().getString(R.string.list_menu_rename))) {
			// If the user selected "Rename"

			LayoutInflater inflater = getLayoutInflater();
			AlertDialog renameDialog = new AlertDialog.Builder(this).setView(inflater.inflate(R.layout.rename_list, null))
					.setTitle(String.format(getResources().getString(R.string.rename_list_title), selectedList.getTitle()))
					.setPositiveButton(R.string.rename_list_okbutton, new DialogInterface.OnClickListener() {
						@Override
						// When the user has pressed "Rename"
						public void onClick(DialogInterface dialog, int id) {
							// Get the input text (the new title)
							EditText newTitleET = (EditText) ((AlertDialog) dialog).findViewById(R.id.new_list_name);
							String newTitle = newTitleET.getText().toString();

							// Check for empty input
							if (newTitle.equals("") || newTitle == null || newTitle.trim().length() == 0) {
								displayToast(getResources().getString(R.string.list_name_empty));
							} else {
								// Try to rename the list
								if (listHelper.renameList(selectedList.getId(), newTitle)) {
									displayToast(String.format(getResources().getString(R.string.list_rename_ok), selectedList.getTitle(), newTitle));
								} else {
									displayToast(String.format(getResources().getString(R.string.list_rename_fail), selectedList.getTitle()));
								}
								
								// Refresh the list view
								updateListView();
							}
						}
					}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						// Cancel button
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					}).create();

			renameDialog.show();
		}

		return true;
	}

}
