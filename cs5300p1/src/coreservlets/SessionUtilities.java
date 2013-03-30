package coreservlets;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.Cookie;

import rpc_layer.DestinationAddressList;

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
	public static Cookie createCookie(String cookieName, String sessionID,
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
	 * @throws UnknownHostException 
	 */
	public static CookieContents readCookie(Cookie cookie) throws NumberFormatException, UnknownHostException{
		if (cookie == null)
			return null;
		String[] contents = cookie.getValue().split(",");
		CookieContents cookieContents = new CookieContents(
				contents[0], Integer.parseInt(contents[1]));
		// this gets the location metadata (if there isn't any, then contents.length == 2)
		// remember Cookies are Strings
		if(contents.length > 2){
			DestinationAddressList destinationAddressList = new DestinationAddressList();
			for (int i=2; i<contents.length; i++){
				parseLocationMetadata(contents[i],destinationAddressList);
			}
			cookieContents.setDestinationAddressList(destinationAddressList);
		}
		return cookieContents;
	}
	
	public static void parseLocationMetadata(String locationMetadata,DestinationAddressList destinationAddressList) throws NumberFormatException, UnknownHostException{
		//TODO: I believe this is incorrect
		String[] IPandPort =locationMetadata.split(":");
		System.out.println(locationMetadata);
		InetAddress ip=InetAddress.getByName(IPandPort[0]);
		System.out.println(IPandPort[0]+ " is "+ip.getHostAddress());
		if(ip.equals(InetAddress.getByName("0.0.0.0"))){
			ip=InetAddress.getByName("127.0.0.1");
		}
		destinationAddressList.addDestAddress(InetAddress.getByName(IPandPort[0]), Integer.parseInt(IPandPort[1]));
	}

	public static UserContents parseReplicatedData(String[] responseString, String sessionID) {
		String[] resp = responseString;
		UserContents replicatedResp = new UserContents(sessionID, Integer.parseInt(resp[2]), resp[3], System.currentTimeMillis() / 1000);
		return replicatedResp;
	}
	public static String getServerID(String responseString) {
		return responseString.split(":")[1];
	}
}
