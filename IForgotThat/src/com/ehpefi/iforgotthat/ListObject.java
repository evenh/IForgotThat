package com.ehpefi.iforgotthat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

/**
 * A list object is one single list in a bigger collection of lists.
 * 
 * @author Even Holthe
 * @since 1.0
 */
public class ListObject {
	private int id;
	private String title;
	private Date timestamp;

	private final SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final String TAG = "ListObject";

	/**
	 * Constructs a new ListObject object with an id, a title and a timestamp
	 * 
	 * @param id The id of the list
	 * @param title The title of the list
	 * @param timestamp The timestamp on which the list was created (String)
	 * 
	 * @since 1.0
	 */
	public ListObject(int id, String title, String timestamp) {
		setId(id);
		setTitle(title);
		setTimestamp(timestamp);
	}

	/**
	 * Constructs a new ListObject object with an id, a title and a timestamp
	 * 
	 * @param id The id of the list
	 * @param title The title of the list
	 * @param timestamp The timestamp on which the list was created (Date object)
	 * 
	 * @since 1.0
	 */
	public ListObject(int id, String title, Date timestamp) {
		setId(id);
		setTitle(title);
		setTimestamp(timestamp);
	}

	@Override
	public int hashCode() {
		final int prime = 101;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
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
		if (!(obj instanceof ListObject)) {
			return false;
		}
		ListObject other = (ListObject) obj;
		if (id != other.id) {
			return false;
		}
		if (timestamp == null) {
			if (other.timestamp != null) {
				return false;
			}
		} else if (!timestamp.equals(other.timestamp)) {
			return false;
		}
		if (title == null) {
			if (other.title != null) {
				return false;
			}
		} else if (!title.equals(other.title)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format("ListObject [id=%s, title=%s, timestamp=%s]", id, title, getTimestampAsString());
	}

	public int getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public String getTimestampAsString() {
		return dtFormat.format(timestamp);
	}

	private void setId(int id) {
		this.id = id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public void setTimestamp(String timestamp) {
		try {
			setTimestamp(dtFormat.parse(timestamp));
		} catch (ParseException e) {
			Log.e(TAG, "Could not interepet the incoming String date '" + timestamp + "' to a Date object!", e);
		}
	}
}
