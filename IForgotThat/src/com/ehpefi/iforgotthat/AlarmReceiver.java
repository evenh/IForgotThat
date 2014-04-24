package com.ehpefi.iforgotthat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Receives notifications from the system and creates notifications accordingly
 * 
 * @author Even Holthe
 * @since 1.0.0
 */
public class AlarmReceiver extends BroadcastReceiver {
	private static final String TAG = "BroadcastReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		// Get extras
		Bundle extras = intent.getExtras();

		Log.d(TAG, "ID recieved: " + extras.getInt("id"));

		// Get the reminder
		ListElementHelper leh = new ListElementHelper(context);
		ListElementObject reminder = leh.getListElement(extras.getInt("id"));
		String description = (reminder.getDescription().equals("") ? context.getResources().getString(R.string.has_no_description) : reminder.getDescription());

		// Get a notification manager
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		// Create the intent which will show the reminder
		Intent reminderToShow = new Intent(context, DetailedReminderActivity.class);
		reminderToShow.putExtra("id", extras.getInt("id"));
		reminderToShow.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pIntent = PendingIntent.getActivity(context, reminder.getId(), reminderToShow, PendingIntent.FLAG_UPDATE_CURRENT);

		// Build notification
		Notification n = new Notification.Builder(context).setContentTitle(context.getResources().getString(R.string.app_name)).setContentText(description)
				.setSmallIcon(R.drawable.ic_launcher).setContentIntent(pIntent).setAutoCancel(true).build();

		// Bells n' whistles - literally
		n.defaults |= Notification.DEFAULT_VIBRATE;
		n.defaults |= Notification.DEFAULT_LIGHTS;
		n.defaults |= Notification.DEFAULT_SOUND;

		// Send off the notification
		notificationManager.notify(reminder.getId(), n);
	}

}
