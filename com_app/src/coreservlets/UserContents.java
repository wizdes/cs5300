package coreservlets;

import coreservlets.DataContents;

public class UserContents extends DataContents {
	private String message = new String();
	private long timeInSeconds = 0;
	public UserContents(int sessionID, int versionNumber, String message, long timeInSeconds){
		super(sessionID, versionNumber);
		this.message = message;
		this.timeInSeconds = timeInSeconds;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public long getTimeInSeconds() {
		return timeInSeconds;
	}
	public void setTimeInSeconds(long timeInSeconds) {
		this.timeInSeconds = timeInSeconds;
	}
	
}
