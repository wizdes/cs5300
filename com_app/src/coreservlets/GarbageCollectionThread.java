package coreservlets;

import java.util.Iterator;
import java.util.concurrent.ConcurrentMap;

import coreservlets.UserContents;

public class GarbageCollectionThread extends Thread{
	private ConcurrentMap<Integer, UserContents> session_info;
	boolean collect;
	public GarbageCollectionThread(ConcurrentMap<Integer, UserContents> session_info){
		this.session_info=session_info;
		setDaemon(true);
		collect=true;
	}
	public void run(){
		while(collect){
			System.out.println("Collecting");
		    for(Iterator<ConcurrentMap.Entry<Integer, UserContents>> iterator 
		    		= session_info.entrySet().iterator();
		    		iterator.hasNext();){
		    	ConcurrentMap.Entry<Integer, UserContents> entry = iterator.next();
			    	if((entry.getValue()).getTimeInSeconds() < System.currentTimeMillis()/1000){
						System.out.println("removing "+entry.getValue().getMessage());
			    		iterator.remove();
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
