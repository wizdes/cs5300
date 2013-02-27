package coreservlets;

public class DataContents{
	private int sessionID;
	private int versionNumber;
	public DataContents(int sessionID, int version_number) {
		this.sessionID = sessionID;
		this.versionNumber = version_number;
	}
	public int getSessionID() {
		return sessionID;
	}
	public void setSessionID(int sessionID) {
		this.sessionID = sessionID;
	}
	public int getVersionNumber() {
		return versionNumber;
	}
	public void setVersionNumber(int versionNumber) {
		this.versionNumber = versionNumber;
	}

	
}