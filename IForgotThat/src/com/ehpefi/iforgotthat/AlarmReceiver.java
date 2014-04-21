package com.ehpefi.iforgotthat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Receives notifications from the system and creates notifications accordingly
 * 
 * @author Even Holthe
 * @since 1.0.0
 */
public class AlarmReceiver extends BroadcastReceiver {
	// Vibration pattern
	long[] pattern = { 800, 0, 400, 0, 800 };

	@Override
	public void onReceive(Context context, Intent intent) {
		// Get extras
		Bundle extras = intent.getExtras();

		// Get the reminder
		ListElementHelper leh = new ListElementHelper(context);
		ListElementObject reminder = leh.getListElement(extras.getInt("id"));
		String description = (reminder.getDescription().equals("") ? context.getResources().getString(R.string.has_no_description) : reminder.getDescription());

		// Get a notification manager
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		// Create the intent which will show the reminder
		Intent reminderToShow = new Intent(context, DetailedReminderActivity.class);
		reminderToShow.putExtra("id", extras.getInt("id"));
		PendingIntent pIntent = PendingIntent.getActivity(context, 0, reminderToShow, 0);

		// Build notification
		Notification n = new Notification.Builder(context).setContentTitle(context.getResources().getString(R.string.app_name)).setContentText(description)
				.setSmallIcon(R.drawable.ic_launcher).setContentIntent(pIntent).setAutoCancel(true).setVibrate(pattern).build();

		// Send off the notification
		notificationManager.notify(0, n);
	}

}
