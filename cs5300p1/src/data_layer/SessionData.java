package data_layer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import coreservlets.UserContents;

public class SessionData {
	public final ReentrantLock counterLock = new ReentrantLock(true);
	// Locks for a given user+session
	public final ConcurrentMap<String, ReentrantLock> sessionLocks = new ConcurrentHashMap<String, ReentrantLock>();
	// User contents
	public final ConcurrentMap<String, UserContents> sessionState = new ConcurrentHashMap<String, UserContents>(); 
	
	public void createNewSession(String sessionID, int versionNumber, String msg, long expDate){
		UserContents insertNew = new UserContents(sessionID, versionNumber, msg, expDate);
		final ReentrantLock insertLock = new ReentrantLock();
		try {
			insertLock.lock();
			sessionLocks.put(sessionID, insertLock);
			sessionState.put(sessionID, insertNew);
		}
		finally{
			insertLock.unlock();
		}		
	}
	
}
