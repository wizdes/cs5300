package coreservlets;

import coreservlets.DataContents;

/**
 * This class extends DataContents to contain a message and a time
 */
public class UserContents extends DataContents {
	private String message = new String();
	private long expirationTime = 0;

	/**
	 * This construct a UserContents object
	 * 
	 * @param sessionID
	 *            The sessionID of this UserContents object
	 * @param versionNumber
	 *            The versionNumber of this UserContents object
	 * @param message
	 *            The message to store in this UserContents object
	 * @param expirationTime
	 *            The expiration time of the UserContents object
	 */
	public UserContents(String sessionID, int versionNumber, String message,
			long expirationTime) {
		super(sessionID, versionNumber);
		this.message = message;
		this.expirationTime = expirationTime;
	}

	/**
	 * @return message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message
	 *            Sets message to message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return expirationTime
	 */
	public long getExpirationTime() {
		return expirationTime;
	}

	/**
	 * @param expirationTime
	 *            Sets expirationTime to expirationTime
	 */
	public void setExpirationTime(long expirationTime) {
		this.expirationTime = expirationTime;
	}

}
