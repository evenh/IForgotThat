package com.ehpefi.iforgotthat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Receives notifications from the system on boot and sets up all the alarms
 * 
 * @author Even Holthe
 * @since 1.0.0
 */
public class BootNotificationReceiver extends BroadcastReceiver {
	private static final String TAG = "BootNotificationReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		// Helpers
		ListHelper lh = new ListHelper(context);
		ListElementHelper leh = new ListElementHelper(context);
		// Create a new alarm manager
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		// Get all lists
		ArrayList<ListObject> allLists = lh.getAllLists(ListHelper.COL_ID);

		// Exit if there are no lists
		if (allLists.size() == 0) {
			Log.i(TAG, "No lists in the database. I am not needed");
			return;
		}

		// Loop through all lists
		for (ListObject list : allLists) {
			// Ensure that we aren't operating on a null object
			if (list != null) {
				// Get all reminders
				ArrayList<ListElementObject> allReminders = leh.getListElementsForListId(list.getId(), ListElementHelper.COL_ID);

				// Exit if there are no lists
				if (allReminders.size() == 0) {
					Log.i(TAG, "No reminders for list #" + list.getId() + ". Moving along to the next list...");
					return;
				}

				// Loop through reminders
				for (ListElementObject reminder : allReminders) {
					// Current time minus 3 minutes
					long currentTime = (new Date().getTime()) - ((60 * 3) * 100);

					// As long as the object isn't null and has a valid alarm
					if (reminder != null && reminder.getAlarmAsString() != ListElementObject.noAlarmString && reminder.isCompleted() == false) {
						if (((reminder.getAlarm().getTime()) - currentTime) > 0) {
							// Temporary calendar object
							Calendar tmp = Calendar.getInstance();
							tmp.setTime(reminder.getAlarm());
							Long time = tmp.getTimeInMillis();

							// Create a new intent
							Intent alarmIntent = new Intent(context, AlarmReceiver.class);
							alarmIntent.putExtra("id", reminder.getId());

							// Set the alarm
							alarmManager.set(AlarmManager.RTC_WAKEUP, time, PendingIntent.getBroadcast(context, 1, alarmIntent, PendingIntent.FLAG_ONE_SHOT));

							Log.d(TAG, "Added alarm for reminder with id " + reminder.getId());
						}
					}
				}
			}
		}
	}

}
