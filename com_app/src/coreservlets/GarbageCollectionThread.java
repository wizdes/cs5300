package coreservlets;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import coreservlets.UserContents;

/**
 * This class implements a garbage collection thread that starts up every
 * minute, walks the sessionState map, and removes all expired sessions.
 */
public class GarbageCollectionThread extends Thread {
	private ConcurrentMap<Integer, UserContents> sessionState;
	private ConcurrentMap<Integer, ReentrantLock> sessionLocks;
	private boolean collect;
	private final static long sleepTime = 60 * 1000;

	/**
	 * The constructs a GarbageCollectionThread object
	 * 
	 * @param sessionState
	 *            The map of session IDs to their session states
	 * @param sessionLocks
	 *            The map of session IDs to their locks
	 */
	public GarbageCollectionThread(
			ConcurrentMap<Integer, UserContents> sessionState,
			ConcurrentMap<Integer, ReentrantLock> sessionLocks) {
		this.sessionState = sessionState;
		this.sessionLocks = sessionLocks;
		setDaemon(true); // Let's us close without needing the thread to end
		collect = true;
	}

	/**
	 * This runs the main GC loop
	 */
	public void run() {
		while (collect) { // This allows us to stop the thread if we need to
			// Get the sessionState keys so we can walk the map
			for (int sessionID : sessionState.keySet()) {
				/** This is basically Test & Test & Set locking */
				// Check if this value has expired, no need to get a lock if it
				// has not
				UserContents userContents = sessionState.get(sessionID);
				if (userContents.getExpirationTime() < System
						.currentTimeMillis() / 1000) {
					// Since we are going to remove the session , lets get the
					// lock
					ReentrantLock removeLock = sessionLocks.get(sessionID);
					try {
						removeLock.lock();
						// Reread post lock
						userContents = sessionState.get(sessionID);
						if (userContents.getExpirationTime() < System
								.currentTimeMillis() / 1000) {
							// If it really is expired remove it and the lock
							sessionState.remove(sessionID);
							sessionLocks.remove(sessionID);
						}
					} finally {
						// Unlock the lock no matter what we did
						removeLock.unlock();
					}
				}
			}
			try {
				// Sleep for sleepTime milliseconds
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {

				System.out.println("We couldn't sleep");
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
