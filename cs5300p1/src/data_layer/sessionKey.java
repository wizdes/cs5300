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
	public int getVersionNumber(){
		return versionNumber;
	}
	
	public void setSessionID(String sessionID){
		this.sessionID = sessionID;
	}

	public void setVersionNumber(int versionNumber){
		this.versionNumber = versionNumber;
	}
    
    public int hashCode(){
        String hashString = sessionID + "_" + Integer.toString(versionNumber);
        return hashString.hashCode();
    }
    
    public boolean equals(Object obj){
        if(!(obj instanceof sessionKey)){
            return false;
        }
        sessionKey other = (sessionKey) obj;
        if(sessionID.equals(other.sessionID) && versionNumber == other.versionNumber){
            return true;
        }
        return false;
    }
	
}
