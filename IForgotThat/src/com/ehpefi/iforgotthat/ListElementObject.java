package com.ehpefi.iforgotthat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Contains information about a specific reminder
 * 
 * @author Even Holthe
 * @since 1.0.0
 */
public class ListElementObject {
	private int id;
	private int listId;
	private String description;
	private Date created;
	private Date alarm;
	private boolean completed;
	private byte[] image;

	public final static String dtFormatString = "yyyy-MM-dd HH:mm:ss";
	public final static String noAlarmString = "0001-01-01 01:01:01";
	private final static SimpleDateFormat dtFormat = new SimpleDateFormat(dtFormatString);
	private static final String TAG = "ListElementObject";

	/**
	 * Constructor for the ListElementObject class.
	 * 
	 * @param id The list element's id
	 * @param listId The list id which the list element is associated with
	 * @param description The user's description of this reminder
	 * @param created A Date object
	 * @param alarm A Date object
	 * @param completed Whether this item is checked off as complete or not
	 * @param image A camera image
	 * @since 1.0.0
	 */
	public ListElementObject(int id, int listId, String description, Date created, Date alarm, boolean completed, byte[] image) {
		this.id = id;
		this.listId = listId;
		this.description = description;
		this.created = created;
		this.alarm = alarm;
		this.completed = completed;
		this.image = image;
	}

	/**
	 * Constructor for the ListElementObject class using Strings instead of Date objects.
	 * 
	 * @param id The list element's id
	 * @param listId The list id which the list element is associated with
	 * @param description The user's description of this reminder
	 * @param created A date (String) in the format of "yyyy-MM-dd HH:mm:ss"
	 * @param alarm A date (String) in the format of "yyyy-MM-dd HH:mm:ss"
	 * @param completed Whether this item is checked off as complete or not
	 * @param image A camera image
	 * @since 1.0.0
	 */
	public ListElementObject(int id, int listId, String description, String created, String alarm, boolean completed, byte[] image) {
		this.id = id;
		this.listId = listId;
		this.description = description;
		setCreated(created);
		setAlarm(alarm);
		this.completed = completed;
		this.image = image;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alarm == null) ? 0 : alarm.hashCode());
		result = prime * result + (completed ? 1231 : 1237);
		result = prime * result + ((created == null) ? 0 : created.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((dtFormat == null) ? 0 : dtFormat.hashCode());
		result = prime * result + id;
		result = prime * result + Arrays.hashCode(image);
		result = prime * result + listId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ListElementObject)) {
			return false;
		}
		ListElementObject other = (ListElementObject) obj;
		if (alarm == null) {
			if (other.alarm != null) {
				return false;
			}
		} else if (!alarm.equals(other.alarm)) {
			return false;
		}
		if (completed != other.completed) {
			return false;
		}
		if (created == null) {
			if (other.created != null) {
				return false;
			}
		} else if (!created.equals(other.created)) {
			return false;
		}
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (dtFormat == null) {
			if (ListElementObject.dtFormat != null) {
				return false;
			}
		} else if (!dtFormat.equals(other.dtFormat)) {
			return false;
		}
		if (id != other.id) {
			return false;
		}
		if (!Arrays.equals(image, other.image)) {
			return false;
		}
		if (listId != other.listId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		boolean hasImage = (image == null ? false : true);

		return String.format("ListElementObject [id=%s, listId=%s, description=%s, created=%s, alarm=%s, completed=%s hasImage=%s]", id, listId, description, getCreatedAsString(),
				getAlarmAsString(), completed, Boolean.toString(hasImage));
	}

	public int getId() {
		return id;
	}

	public int getListId() {
		return listId;
	}

	public String getDescription() {
		return description;
	}

	public Date getCreated() {
		return created;
	}

	public String getCreatedAsString() {
		return dtFormat.format(created);
	}

	public Date getAlarm() {
		return alarm;
	}

	public String getAlarmAsString() {
		try {
			return dtFormat.format(alarm);
		} catch (NullPointerException npe) {
			return noAlarmString;
		}
	}

	public static String getDateAsString(Date date) {
		if (date == null) {
			return noAlarmString;
		}
		return dtFormat.format(date);
	}

	public boolean isCompleted() {
		return completed;
	}

	public byte[] getImage() {
		return image;
	}

	public Bitmap getImageAsBitmap() {
		return BitmapFactory.decodeByteArray(image, 0, image.length);
	}

	public void setListId(int listId) {
		this.listId = listId;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public void setCreated(String created) {
		try {
			setCreated(dtFormat.parse(created));
		} catch (ParseException e) {
			Log.e(TAG, "Could not interepet the incoming String date '" + created + "' to a Date object!", e);
		}
	}

	public void setAlarm(Date alarm) {
		this.alarm = alarm;

	}

	public void setAlarm(String alarm) {
		try {
			setAlarm(dtFormat.parse(alarm));
		} catch (ParseException e) {
			try {
				setAlarm(dtFormat.parse(noAlarmString));
			} catch (ParseException pe) {
				Log.e(TAG, "We failed miserably!");
			}
		} catch (NullPointerException npe) {
			try {
				setAlarm(dtFormat.parse(noAlarmString));
			} catch (ParseException e) {
				Log.e(TAG, "We failed miserably :(");
			}
		}
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}

	/**
	 * Schedules an alarm for the currenct object
	 * 
	 * @param context The calling class
	 * @since 1.0.0
	 */
	public void registerAlarm(Context context) {
		// If we have a valid alarm
		if (alarm != null && !getAlarmAsString().equals(noAlarmString)) {
			// Temporary calendar object
			Calendar tmp = Calendar.getInstance();
			tmp.setTime(alarm);
			Long time = tmp.getTimeInMillis();

			// Create a new intent for the alarm receiver
			Intent alarmIntent = new Intent(context, AlarmReceiver.class);
			alarmIntent.putExtra("id", getId());

			// Alarm manager
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

			// Cancel any previous alarms and set a new alarm
			alarmManager.cancel(PendingIntent.getBroadcast(context, getId(), alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT));
			alarmManager.set(AlarmManager.RTC_WAKEUP, time, PendingIntent.getBroadcast(context, getId(), alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT));

			Log.d(TAG, "Enabled alarm for reminder #" + getId());
		}
	}

	public void cancelAlarm(Context context) {
		// If we have a valid alarm
		if (alarm != null && !getAlarmAsString().equals(noAlarmString)) {
			// Create a new intent for the alarm receiver
			Intent alarmIntent = new Intent(context, AlarmReceiver.class);
			alarmIntent.putExtra("id", getId());

			// Alarm manager
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

			// Cancel this alarm
			alarmManager.cancel(PendingIntent.getBroadcast(context, getId(), alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT));
			notificationManager.cancel(getId());

			Log.d(TAG, "Canceled alarm for reminder #" + getId());
		} else {
			Log.d(TAG, "No alarm to cancel for reminder #" + getId());
		}
	}
}
