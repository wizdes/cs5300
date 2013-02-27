package coreservlets;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/my-response")
public class MyResponse extends HttpServlet {
	//Synchronization Variables
	private final ReentrantLock lockCreationLock = new ReentrantLock(true);	//Used when creating cookies to protect session id, etc
	private final ConcurrentMap<Integer, ReentrantLock> sessionLocks = new ConcurrentHashMap<>();	//Locks for a given user/session
	private final ConcurrentMap<Integer, UserContents> sessionInfo = new ConcurrentHashMap<>();		//User contents
	
	
	private int counter = 0;
	private final String StandardCookieName = "CS5300PROJ1SESSION";
	public final int time_out_secs = 5;
	private GarbageCollectionThread garbageCollectionThread;
	 // This Happens Once and is Reused
    public void init(ServletConfig config) throws ServletException
    {
    	super.init(config); 
    	garbageCollectionThread = new GarbageCollectionThread(sessionInfo);
    	garbageCollectionThread.start();
    }
    

    /**
     * Creates a cookie. Assumes the session lock has been acquired.
     * @param sessionID
     * @param versionNumber
     * @param message
     * @param lm
     * @return
     */
    private Cookie createCookie(int sessionID, int versionNumber, String message, String lm){
    	Cookie retCookie = SessionUtilities.createCookie(StandardCookieName, sessionID, versionNumber, lm);
    	UserContents userContents = new UserContents(sessionID, versionNumber, new String(message), System.currentTimeMillis()/1000 + (long)time_out_secs );
    	sessionInfo.put(counter, userContents);
    	return retCookie;
    }
    public synchronized Cookie modCounterCreateCookie(int versionNumber, String message, String lm){
    	Cookie retCookie = SessionUtilities.createCookie(StandardCookieName, counter, versionNumber, lm);
    	UserContents userContents = new UserContents(counter, versionNumber, new String(message), System.currentTimeMillis()/1000 + (long)time_out_secs );
    	sessionInfo.put(counter, userContents);
    	counter += 1;
    	return retCookie;
    }
    
    public void modState(CookieContents cookieContents, HttpServletResponse response, String msg){
		response.addCookie(SessionUtilities.createCookie(StandardCookieName, cookieContents.getSessionID(), cookieContents.getVersionNumber() + 1, cookieContents.getLm()));
    	UserContents uc = new UserContents(cookieContents.getSessionID(), cookieContents.getVersionNumber(), new String(msg), System.currentTimeMillis()/1000 + (long)time_out_secs);
    	sessionInfo.put(cookieContents.getSessionID(), uc);
    }
    
	void printWebsite(String msg, PrintWriter out, CookieContents cc){
		out.print("<big><big><b>\n" + msg + "</b></big></big>\n");
		out.print("<html>\n" +
    			"<body>\n" +
    			"<form method=GET action=\"my-response\">" +
				"<input type=text name=NewText size=30 maxlength=512>" +
				"&nbsp;&nbsp;" +
				"<input type=submit name=cmd value=Save>" +
				"&nbsp;&nbsp;" +
				"</form>" +
				"<form method=GET action=\"my-response\">" +
				"<input type=submit name=REF value=Refresh>" +
				"</form>" +
				"<form method=GET action=\"my-response\">" +
				"<input type=submit name=ESC value=LogOut>" +
				"</form>" +
				"</body>" +
	    		"</html>");
	}
	/**
	 * 
	 * @param cookieContents
	 * @param cookieLock
	 */
	private int acquireLock(CookieContents cookieContents) {
		int sessionID;
		// Create lock if one doesn't exist for this cookie and lock it
		if (cookieContents == null
				|| !sessionLocks.containsKey(cookieContents.getSessionID())) { // atomic
			ReentrantLock sessionLock = null;
			try {
				lockCreationLock.lock();
				sessionID = counter++;

				// Create the lock
				sessionLock = sessionLocks.get(sessionID);

				if (sessionLock == null) { // Check again since might have
											// been created
					// Create lock
					sessionLock = new ReentrantLock(true);

					// Lock map can't contain this session id if done
					// properly
					sessionLocks.put(sessionID, sessionLock);
				}
			} finally {
				sessionLock.lock();
				lockCreationLock.unlock();
			}
		} else {
			sessionID = cookieContents.getSessionID();
			sessionLocks.get(sessionID).lock();
		}
		return sessionID;
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		CookieContents cookieContents = SessionUtilities
				.readCookie(SessionUtilities.GetRequestCookie(
						StandardCookieName, request.getCookies()));
		// ????Check for exp here??

		int sessionID = -1;
		try{
			//Condition: Cookie is now locked and the cookieCreationLock has been released
			sessionID = acquireLock(cookieContents);
			//Process Cookie
			if (!sessionInfo.containsKey(sessionID)) {
				// Create new cookie
				createCookie(sessionID, 0, "Default Message.", "");
				return;
			} else { // The cookie exists so process it
				Enumeration<String> paramNames = request.getParameterNames();
				while (paramNames.hasMoreElements()) {
					String paramName = paramNames.nextElement();
					String[] paramValues = request.getParameterValues(paramName);

					if (paramValues.length == 1 && paramName.equals("REF")) {
						modState(cookieContents, response,
								sessionInfo.get(cookieContents.getSessionID())
										.getMessage());
						break;
					}

					if (paramValues.length == 1 && paramName.equals("NewText")) {
						String paramValue = paramValues[0];
						modState(cookieContents, response, paramValue);
						break;
					}

					if (paramValues.length == 1 && paramName.equals("ESC")) {	//Logout/Remove
						printWebsite("Default Message.", out, cookieContents);
						sessionInfo.remove(cookieContents.getSessionID());
						//lock remove
						return;
					}
				}
			}
			printWebsite(sessionInfo.get(sessionID).getMessage(), out, cookieContents);
		} finally {
			sessionLocks.get(sessionID).unlock();
		}
	}

//	@Override
//	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//	    response.setContentType("text/html");
//	    PrintWriter out = response.getWriter();
//		
//	    //????Check for exp here??
//		// first time cookie and website handling here
//	    CookieContents cookieContents = SessionUtilities.readCookie(SessionUtilities.GetRequestCookie(StandardCookieName,request.getCookies()));
//		if(cookieContents == null || sessionInfo.get(cookieContents.getSessionID()) == null){
//			Cookie resp_cookie = modCounterCreateCookie(0, "Default Message.", "");
//		    response.addCookie(resp_cookie);
//		    printWebsite("Default Message.", out, cookieContents);
//		    return;
//		}
//		
//		Enumeration<String> paramNames = request.getParameterNames();
//	    while(paramNames.hasMoreElements()) {
//	    	String paramName = paramNames.nextElement();
//	    	String[] paramValues = request.getParameterValues(paramName);
//	    	
//	    	if (paramValues.length == 1 && paramName.equals("REF")){
//	    		modState(cookieContents, response, sessionInfo.get(cookieContents.getSessionID()).getMessage());
//	    		break;
//	    	}
//	    	
//	    	if (paramValues.length == 1 && paramName.equals("NewText")) {
//	    		String paramValue = paramValues[0];
//	    		modState(cookieContents, response, paramValue);
//	    		break;
//	    	}	    
//	    	
//	    	if (paramValues.length == 1 && paramName.equals("ESC")){
//	    		printWebsite("Default Message.", out, cookieContents);
//	    		sessionInfo.remove(cookieContents.getSessionID());
//	    		return;
//	    	}
//    	}
//    
//	    printWebsite(sessionInfo.get(cookieContents.getSessionID()).getMessage(), out, cookieContents);
//	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
