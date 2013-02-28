package coreservlets;

/**
 * This class stores the session objects and their version numbers
 */
public class DataContents {
	private int sessionID;
	private int versionNumber;

	/**
	 * Constructor for DataContents
	 * 
	 * @param sessionID
	 *            The sessionID of this DataContents Object
	 * @param versionNumber
	 *            The version number of this DataContents Object
	 */
	public DataContents(int sessionID, int versionNumber) {
		this.sessionID = sessionID;
		this.versionNumber = versionNumber;
	}

	/**
	 * @return seesionID
	 */
	public int getSessionID() {
		return sessionID;
	}

	/**
	 * @param sessionID
	 *            Sets sessionID to sessionID
	 */
	public void setSessionID(int sessionID) {
		this.sessionID = sessionID;
	}

	/**
	 * @return versionNumber
	 */
	public int getVersionNumber() {
		return versionNumber;
	}

	/**
	 * @param versionNumber
	 *            Sets versionNumber to versionNumber
	 */
	public void setVersionNumber(int versionNumber) {
		this.versionNumber = versionNumber;
	}

}