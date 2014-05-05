package com.ehpefi.iforgotthat;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.ehpefi.iforgotthat.swipelistview.BaseSwipeListViewListener;
import com.ehpefi.iforgotthat.swipelistview.SwipeListView;

/**
 * The main activity which displays all the available lists
 * 
 * @author Per Erik Finstad
 * @author Even Holthe
 * @since 1.0.0
 */
public class MainActivity extends Activity {
	// UI Elements
	SwipeListView listView;
	EditText listNameInput;
	TextView hasNoLists;
	Button showCompletedReminders;

	// Utilities
	ViewSwitcher switcher;
	public int position;
	InputMethodManager imm;

	// Database related
	ListHelper listHelper;
	ListElementHelper listElementHelper;
	ArrayList<ListObject> allLists;
	ListObjectAdapter listAdapter;

	// Used for logging
	private static final String TAG = "MainActivity";

	// Notification
	Toast toast;

	// Context of this class
	final Context context = this;
	ListObject currentList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Find our UI elements
		listView = (SwipeListView) findViewById(R.id.mainView);
		listNameInput = (EditText) findViewById(R.id.editable_add_list_input);
		hasNoLists = (TextView) findViewById(R.id.text_has_no_lists);
		switcher = (ViewSwitcher) findViewById(R.id.add_list_switcher);
		showCompletedReminders = (Button) findViewById(R.id.btn_show_completed);

		// Initialize helpers
		listHelper = new ListHelper(this);
		listElementHelper = new ListElementHelper(this);
		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		// Fetch all the lists from the database
		allLists = listHelper.getAllLists(ListHelper.COL_ID);

		// Create a new list adapter for all our lists
		listAdapter = new ListObjectAdapter(this, R.layout.list_row, allLists);
		listView.setAdapter(listAdapter);

		// Swipe listener
		listView.setSwipeListViewListener(new BaseSwipeListViewListener() {
			// Whenever a row has been slided to show the back, update the list position
			@Override
			public void onOpened(int pos, boolean toRight) {
				position = pos;
				Log.d(TAG, "Updated list position to " + pos);
			}

			// For clicks on the list names
			@Override
			public void onClickFrontView(int position) {
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

		showLists();
	}

	/**
	 * Takes care of showing an empty message if there are no lists, and all the lists if number of lists > 0
	 * 
	 * @since 1.0.0
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

		Log.d(TAG, "Lists in the datbase");

		// Show the list view if we have data
		hasNoLists.setVisibility(View.GONE);
		listView.setVisibility(View.VISIBLE);

		// Show the completed reminders when completed>=1
		if (listElementHelper.getCompletedItems().size() >= 1) {
			showCompletedReminders.setVisibility(View.VISIBLE);
		} else {
			showCompletedReminders.setVisibility(View.GONE);
		}

	}

	/**
	 * Shows the completed items
	 * 
	 * @param view
	 *            The view
	 * @since 1.0.0
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
	 * @param view
	 *            The view
	 * @since 1.0.0
	 */
	public void addNewListClicked(View view) {
		// Show the title name input
		switcher.showNext();

		// Request focus
		listNameInput.requestFocus();

		// If we have focus, show the keyboard
		if (listNameInput.hasFocus()) {
			imm.toggleSoftInputFromWindow(listNameInput.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
		}
	}

	/**
	 * Convenience method for displaying a toast message
	 * 
	 * @param message
	 *            The message to display
	 * @since 1.0.0
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
	 * Handles clicks on the "delete list" button
	 * 
	 * @param view
	 *            The view
	 * @since 1.0.0
	 */
	public void deleteList(View view) {
		// Get the object to be deleted
		currentList = listAdapter.getItem(position);

		// Create a dialog box to confirm deletion
		AlertDialog confirmDeletion = new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.list_deletion_title))
				.setMessage(String.format(getResources().getString(R.string.list_deletion_confirm), currentList.getTitle()))
				.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// If we successfully deleted the list
						if (listHelper.deleteList(currentList.getId(), context)) {
							// Close the slider
							listView.closeAnimate(position);

							// Remove the list from the array
							allLists.remove(position);

							displayToast(String.format(getResources().getString(R.string.list_deletion_ok), currentList.getTitle()));
						} else {
							// Couldn't delete the list
							displayToast(String.format(getResources().getString(R.string.list_deletion_fail), currentList.getTitle()));
						}

						// Refresh view
						listAdapter.notifyDataSetChanged();

						showLists();
					}
				}).setNegativeButton(getResources().getString(R.string.no), new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						listView.closeAnimate(position);
						dialog.cancel();
					}
				}).create();

		confirmDeletion.show();
	}

	public void renameList(View view) {
		// Get the object to be renamed
		currentList = listAdapter.getItem(position);

		// Inflate layout
		View renameLayout = getLayoutInflater().inflate(R.layout.rename_list, null);
		// Set text inside the EditText (the current list name)
		((EditText) renameLayout.findViewById(R.id.new_list_name)).setText(currentList.getTitle());

		AlertDialog renameDialog = new AlertDialog.Builder(this).setView(renameLayout)
				.setTitle(String.format(getResources().getString(R.string.rename_list_title), currentList.getTitle()))
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
							if (listHelper.renameList(currentList.getId(), newTitle)) {
								displayToast(getResources().getString(R.string.list_rename_ok));

								// Rename the local object
								currentList.setTitle(newTitle);

								// Close the slider
								listView.closeAnimate(position);

								// Update the dataset
								listAdapter.notifyDataSetChanged();
								showLists();
							} else {
								displayToast(String.format(getResources().getString(R.string.list_rename_fail), currentList.getTitle()));
							}
						}
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					// Cancel button
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						// Close the slider
						listView.closeAnimate(position);
					}
				}).create();

		renameDialog.show();
	}

	/**
	 * Handle the user input after they have provided a list name
	 * 
	 * @param view
	 *            The view
	 * @since 1.0.0
	 */
	public void addNewListTextEntered(View view) {
		// The list name the user typed in
		String inputName = listNameInput.getText().toString();
		Log.d(TAG, "The list name that was entered was: " + inputName);

		if (inputName.equals("") || inputName == null || inputName.trim().length() == 0) {
			// Send toast to user
			displayToast(getResources().getString(R.string.list_name_empty));

			// Dismiss keyboard and switch back to text saying "Add new list"
			imm.hideSoftInputFromWindow(listNameInput.getWindowToken(), 0);
			switcher.showNext();

			return;
		}

		// Check for a list named the same as the input
		if (listHelper.doesListWithTitleExist(inputName)) {
			displayToast(String.format(getResources().getString(R.string.list_already_exists), inputName));
		} else {
			// Try to create the list
			int listID = listHelper.createNewList(inputName);

			if (listID > 0) {
				// All good, update the data and tell the listAdapter to refresh
				allLists.add(listHelper.getList(listID));
				listAdapter.notifyDataSetChanged();
				showLists();
				// Tell the user that everything went OK
				displayToast(String.format(getResources().getString(R.string.list_creation_ok), inputName));
				// Close the keyboard
				imm.hideSoftInputFromWindow(listNameInput.getWindowToken(), 0);
				// Show the add new list text
				switcher.showNext();
				// Reset the text in the EditText
				listNameInput.setText("");
			} else {
				// Just display a toast telling the user that the creation of a
				// new list failed
				displayToast(String.format(getResources().getString(R.string.list_creation_fail), inputName));
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

}
