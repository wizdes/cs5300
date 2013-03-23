package data_layer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import coreservlets.UserContents;


public class SessionData {
	public final ReentrantLock counterLock = new ReentrantLock(true);
	// User contents
	public final ConcurrentMap<sessionKey, UserContents> sessionState = new ConcurrentHashMap<sessionKey, UserContents>(); 
	
	public void createNewSession(String sessionID, int versionNumber, String msg, long expDate){
		UserContents insertNew = new UserContents(sessionID, versionNumber, msg, expDate);
		sessionState.put(new sessionKey(sessionID, versionNumber), insertNew);
	}
	
}
