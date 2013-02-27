package coreservlets;

import coreservlets.DataContents;

public class UserContents extends DataContents {
	private String message = new String();
	private long time_in_secs = 0;
	public UserContents(int sessionID, int version_number, String message, long time_in_secs){
		super(sessionID, version_number);
		this.message = message;
		this.time_in_secs = time_in_secs;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public long getTime_in_secs() {
		return time_in_secs;
	}
	public void setTime_in_secs(long time_in_secs) {
		this.time_in_secs = time_in_secs;
	}
	
}
