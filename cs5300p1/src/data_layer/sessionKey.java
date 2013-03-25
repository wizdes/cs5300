package data_layer;

public class sessionKey {
	String sessionID;
	int versionNumber;
	public sessionKey(){
		
	}
	public sessionKey(String sessionID, int versionNumber){
		this.sessionID = sessionID;
		this.versionNumber = versionNumber;
	}
	
	public String getSessionID(){
		return sessionID;
	}
	public int versionNumber(){
		return versionNumber;
	}
	
	public void setSessionID(String sessionID){
		this.sessionID = sessionID;
	}

	public void setVersionNumber(int versionNumber){
		this.versionNumber = versionNumber;
	}
	
}
