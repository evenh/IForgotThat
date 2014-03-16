package com.ehpefi.iforgotthat;

/**
 * A list object is one single list in a bigger collection of lists.
 * 
 * @author Even Holthe
 * @since 1.0
 */
public class ListObject {
	private int id;
	private String title;
	private String timestamp;

	/**
	 * Constructs a new ListObject object with an id, a title and a timestamp
	 * 
	 * @param id The id of the list
	 * @param title The title of the list
	 * @param timestamp The timestamp on which the list was created
	 * 
	 * @since 1.0
	 */
	public ListObject(int id, String title, String timestamp) {
		setId(id);
		setTitle(title);
		setTimestamp(timestamp);
	}

	public int getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "ID: " + getId() + "\nTitle: " + getTitle() + "\nTimestamp: "
				+ getTimestamp() + "\n\n";
	}

}
