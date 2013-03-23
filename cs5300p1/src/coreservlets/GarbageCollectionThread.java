package coreservlets;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import coreservlets.UserContents;
import data_layer.sessionKey;

/**
 * This class implements a garbage collection thread that starts up every
 * minute, walks the sessionState map, and removes all expired sessions.
 */
public class GarbageCollectionThread extends Thread {
	private ConcurrentMap<sessionKey, UserContents> sessionState;
	private boolean collect;
	private final static long sleepTime = 60 * 1000;

	/**
	 * The constructs a GarbageCollectionThread object
	 * 
	 * @param sessionState2
	 *            The map of session IDs to their session states
	 * @param sessionLocks2
	 *            The map of session IDs to their locks
	 */
	public GarbageCollectionThread(
			ConcurrentMap<sessionKey, UserContents> sessionState) {
		this.sessionState = sessionState;
		setDaemon(true); // Let's us close without needing the thread to end
		collect = true;
	}

	/**
	 * This runs the main GC loop
	 */
	public void run() {
		while (collect) { // This allows us to stop the thread if we need to
			// Get the sessionState keys so we can walk the map
			System.out.println("Collecting");
			for (sessionKey sessionID : sessionState.keySet()) {
				/** This is basically Test & Test & Set locking */
				// Check if this value has expired, no need to get a lock if it
				// has not
				UserContents userContents = sessionState.get(sessionID);
				if (userContents.getExpirationTime() < System.currentTimeMillis() / 1000) {
					// remove the session
					sessionState.remove(sessionID);
				}
			}
			try {
				// Sleep for sleepTime milliseconds
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {

				System.out.println("We couldn't sleep.");
				e.printStackTrace();
			}
		}
	}

	/**
	 * This function sets collect to false, which will stop the thread on the
	 * next loop iteration
	 */
	public void shutdown() {
		collect = false;
	}
}
