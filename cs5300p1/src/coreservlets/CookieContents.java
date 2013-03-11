package coreservlets;

/**
 * This object extends DataContents to contain cookie information; it adds
 * locationMetadata.
 */
public class CookieContents extends DataContents {
	private String locationMetadata;

	/**
	 * Constructs a CookieContents object
	 * 
	 * @param sessionID
	 *            The sessionID of the cookie
	 * @param versionNumber
	 *            The versionNumber of the cookie
	 * @param locationMetadata
	 *            The locationMetadata of the cookie
	 */
	public CookieContents(String sessionID, int versionNumber,
			String locationMetadata) {
		super(sessionID, versionNumber);
		this.locationMetadata = locationMetadata;
	}

	/**
	 * @return locationMetadata
	 */
	public String getLocationMetadata() {
		return locationMetadata;
	}

	/**
	 * @param locationMetadata
	 *            Sets locationMetadata to locationMetadata
	 */
	public void setLocationMetadata(String locationMetadata) {
		this.locationMetadata = locationMetadata;
	}

}
