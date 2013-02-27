package coreservlets;

import javax.servlet.http.Cookie;

public class SessionUtilities {

	public static Cookie createCookie(String cookieName, int sessionID, int version_number, String lm){

		Cookie retCookie =  new Cookie(cookieName, sessionID + "," + version_number + "," + lm);
		return retCookie;
	}
    public static Cookie GetRequestCookie(String cookieName, Cookie[] cookies){
    	if(cookies == null) return null;
    	for(Cookie cookie:cookies){
    		if(cookie.getName().equals(cookieName)){
    			return cookie;
    		}
    	}
    	return null;
    }
    
	
	public static CookieContents readCookie(Cookie cookie) {
		if(cookie == null) return null;
		String[] contents = cookie.getValue().split(",");
		CookieContents cookieContents = new CookieContents(Integer.parseInt(contents[0]),Integer.parseInt(contents[1]), "");
		cookieContents.setLm((contents.length > 2) ? contents[2] : "");
		return cookieContents;
	}
}
