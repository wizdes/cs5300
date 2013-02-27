package coreservlets;

public class DataContents{
	private int sessionID;
	private int version_number;
	public DataContents(int sessionID, int version_number) {
		this.sessionID = sessionID;
		this.version_number = version_number;
	}
	public int getSessionID() {
		return sessionID;
	}
	public void setSessionID(int sessionID) {
		this.sessionID = sessionID;
	}
	public int getVersion_number() {
		return version_number;
	}
	public void setVersion_number(int version_number) {
		this.version_number = version_number;
	}
	
}