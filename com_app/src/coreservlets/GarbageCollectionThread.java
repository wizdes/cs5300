package coreservlets;

import java.util.Iterator;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import coreservlets.UserContents;

public class GarbageCollectionThread extends Thread{
	private ConcurrentMap<Integer, UserContents> sessionState;
	private ConcurrentMap<Integer, ReentrantLock> sessionLocks;
	boolean collect;
	public GarbageCollectionThread(ConcurrentMap<Integer, UserContents> sessionState,
			ConcurrentMap<Integer, ReentrantLock> sessionLocks){
		this.sessionState=sessionState;
		setDaemon(true);
		collect=true;
	}
	public void run(){
		while(collect){
			System.out.println("Collecting");
			
		    for(int sessionID: sessionState.keySet()){
		    	UserContents val = sessionState.get(sessionID);
		    	if(val.getTimeInSeconds() < System.currentTimeMillis()/1000){
		    		ReentrantLock removeLock = sessionLocks.get(sessionID);
		    		try{
		    			removeLock.lock();
		    			sessionState.remove(sessionID);
		    			sessionLocks.remove(sessionID);
		    		}
		    		finally{
		    			removeLock.unlock();
		    		}
		    	}
		    }
		    try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				System.out.println("We couldn't sleep");
				e.printStackTrace();
			}
		}
	}
	public void shutdown(){
		collect=false;
	}
}
