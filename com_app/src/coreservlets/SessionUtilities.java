package coreservlets;

import javax.servlet.http.Cookie;

/**
 * A list of static classes to do the low level work of cookie creation,
 * reading, and Acquiring
 */
public class SessionUtilities {

	/**
	 * Constructs a Cookie object
	 * 
	 * @param cookieName
	 *            The name of the cookie
	 * @param sessionID
	 *            The sessionID of the cookie
	 * @param versionNumber
	 *            The versionNumber of the cookie
	 * @param locationMetadata
	 *            The locationMetadata of the cookie
	 * @return The new cookie
	 */
	public static Cookie createCookie(String cookieName, int sessionID,
			int versionNumber, String locationMetadata) {
		return new Cookie(cookieName, sessionID + "," + versionNumber + ","
				+ locationMetadata);
	}

	/**
	 * Returns a cookie with the given name
	 * 
	 * @param cookieName
	 *            The name of the cookie to find
	 * @param cookies
	 *            The list of cookies
	 * @return a cookie with the given name
	 */
	public static Cookie GetRequestCookie(String cookieName, Cookie[] cookies) {
		if (cookies == null)
			return null;
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(cookieName)) {
				return cookie;
			}
		}
		return null;
	}

	/**
	 * Reads a cookie into a CookieContents object
	 * 
	 * @param cookie
	 *            The cookie to read
	 * @return A new CookieContents object that represents the data in cookie
	 */
	public static CookieContents readCookie(Cookie cookie) {
		if (cookie == null)
			return null;
		String[] contents = cookie.getValue().split(",");
		CookieContents cookieContents = new CookieContents(
				Integer.parseInt(contents[0]), Integer.parseInt(contents[1]),
				"");
		cookieContents.setLocationMetadata((contents.length > 2) ? contents[2]
				: "");
		return cookieContents;
	}
}
