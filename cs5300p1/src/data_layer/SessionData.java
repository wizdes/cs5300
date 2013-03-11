package data_layer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import coreservlets.UserContents;

public class SessionData {
	public final ReentrantLock counterLock = new ReentrantLock(true);
	// Locks for a given user+session
	public final ConcurrentMap<Integer, ReentrantLock> sessionLocks = new ConcurrentHashMap<Integer, ReentrantLock>();
	// User contents
	public final ConcurrentMap<Integer, UserContents> sessionState = new ConcurrentHashMap<Integer, UserContents>(); 
}
