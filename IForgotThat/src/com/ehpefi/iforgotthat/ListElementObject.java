package com.ehpefi.iforgotthat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

public class ListElementObject {
	private int id;
	private int listId;
	private String description;
	private Date created;
	private Date alarm;
	private boolean completed;
	private byte[] image;

	private final SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
	 * @since 1.0
	 */
	public ListElementObject(int id, int listId, String description, Date created, Date alarm, boolean completed,
			byte[] image) {
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
	 * @since 1.0
	 */
	public ListElementObject(int id, int listId, String description, String created, String alarm, boolean completed,
			byte[] image) {
		this.id = id;
		this.listId = listId;
		this.description = description;
		setCreated(created);
		setAlarm(alarm);
		this.completed = completed;
		this.image = image;
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
		return dtFormat.format(alarm);
	}

	public boolean isCompleted() {
		return completed;
	}

	public byte[] getImage() {
		return image;
	}

	public void setId(int id) {
		this.id = id;
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
	
	public void setCreated(String created){
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
			setCreated(dtFormat.parse(alarm));
		} catch (ParseException e) {
			Log.e(TAG, "Could not interepet the incoming String date '" + alarm + "' to a Date object!", e);
		}
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}
}
