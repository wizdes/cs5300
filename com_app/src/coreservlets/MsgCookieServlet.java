package coreservlets;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
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

@WebServlet("/msgcookieservlet")
public class MsgCookieServlet extends HttpServlet {
	//Synchronization Variables
	private final ReentrantLock CreationLock = new ReentrantLock(true);	//Used when creating cookies to protect session id, etc
	private final ConcurrentMap<Integer, ReentrantLock> sessionLocks = new ConcurrentHashMap<>();	//Locks for a given user/session
	private final ConcurrentMap<Integer, UserContents> sessionState = new ConcurrentHashMap<>();		//User contents
	
	
	private int counter = 0;
	private final String StandardCookieName = "CS5300PROJ1SESSION";
	public final int time_out_secs = 5;
	private GarbageCollectionThread garbageCollectionThread;
	private final String DefaultMessage = "Default Message.";
	 // This Happens Once and is Reused
    public void init(ServletConfig config) throws ServletException
    {
    	super.init(config); 
    	garbageCollectionThread = new GarbageCollectionThread(sessionState, sessionLocks);
    	garbageCollectionThread.start();
    }

    
	void printWebsite(String msg, PrintWriter out, int sessionID, HttpServletRequest request, long expires){
		out.print("<big><big><b>\n" + msg + "</b></big></big>\n");
		out.print("<html>\n" +
    			"<body>\n" +
    			"<form method=GET action=\"msgcookieservlet\">" +
				"<input type=text name=NewText size=30 maxlength=512>" +
				"&nbsp;&nbsp;" +
				"<input type=submit name=cmd value=Replace>" +
				"&nbsp;&nbsp;" +
				"</form>" +
				"<form method=GET action=\"msgcookieservlet\">" +
				"<input type=submit name=REF value=Refresh>" +
				"</form>" +
				"<form method=GET action=\"msgcookieservlet\">" +
				"<input type=submit name=ESC value=LogOut>" +
				"</form>" +
				"</body>" +
	    		"</html>");
		out.print("Session on: " + request.getRemoteAddr() + ":" + request.getRemotePort() + "<br/>");
		out.print("Expires " + new Date(expires * 1000));

	}
	
	void createCookie(HttpServletRequest request, PrintWriter out, HttpServletResponse resp){
		int sessionID = -1;
		try{
			CreationLock.lock();
			sessionID = counter++;
		}
		finally{
			CreationLock.unlock();
		}
		
		long expiration_date = System.currentTimeMillis()/1000 + (long)time_out_secs;
		UserContents insertNew = new UserContents(sessionID, 0, DefaultMessage, expiration_date);
		Cookie retCookie = SessionUtilities.createCookie(StandardCookieName, sessionID, 0, "");
		resp.addCookie(retCookie);
		
		final ReentrantLock insertLock = new ReentrantLock();
		try{
			insertLock.lock();
			sessionLocks.put(sessionID, insertLock);
			sessionState.put(sessionID, insertNew);
			printWebsite(DefaultMessage, out, sessionID, request, expiration_date);
		}
		finally{
			insertLock.unlock();
		}
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		CookieContents cookieContents = SessionUtilities
				.readCookie(SessionUtilities.GetRequestCookie(
						StandardCookieName, request.getCookies()));
		
		if(cookieContents == null){
			createCookie(request, out, response);
			return;
		}
		
		
		
		int sessionID = cookieContents.getSessionID();
		ReentrantLock SessionLock = sessionLocks.get(sessionID);
		if(SessionLock == null){
			createCookie(request, out, response);
			return;			
		}
		try{
			SessionLock.lock();
			UserContents session_info = sessionState.get(sessionID);
			
			if(session_info == null 
					|| session_info.getTimeInSeconds() < System.currentTimeMillis()/1000){
				createCookie(request, out, response);
				return;			
			}
			process_session(request, sessionID, response);
			printWebsite(sessionState.get(sessionID).getMessage(), out, sessionID, request, 
					sessionState.get(sessionID).getTimeInSeconds());
		}
		finally{
			SessionLock.unlock();
		}
		return;
	}

	public void modState(int sessionID, int versionNum, String lm,
			HttpServletResponse response, String msg) {
		response.addCookie(SessionUtilities.createCookie(StandardCookieName, sessionID,versionNum + 1, lm));
		UserContents uc = new UserContents(sessionID, versionNum + 1, new String(msg),
				System.currentTimeMillis() / 1000 + (long) time_out_secs);
		sessionState.put(sessionID, uc);
	}
	
	void process_session(HttpServletRequest request, int sessionID, HttpServletResponse response){
		int versionNum =  sessionState.get(sessionID).getVersionNumber();
		String msg = sessionState.get(sessionID).getMessage();
		Enumeration<String> paramNames = request.getParameterNames();
		 while (paramNames.hasMoreElements()) {
			 String paramName = paramNames.nextElement();
			 String[] paramValues = request.getParameterValues(paramName);
			 
			 System.out.println(paramName);
	
			 if (paramValues.length == 1 && paramName.equals("REF")) {
				 modState(sessionID, versionNum, "", response, msg);
				 break;
			 }
	
			 if (paramValues.length == 1 && paramName.equals("NewText")) {
				 String paramValue = paramValues[0]; 
				 System.out.println(paramValue);
				 modState(sessionID, versionNum, "", response, paramValue);
			 break;
			 }
	
			 if (paramValues.length == 1 && paramName.equals("ESC")) { //Logout/Remove
				 sessionState.get(sessionID).setTimeInSeconds(System.currentTimeMillis()/1000 - 1);				 
			 //lock remove
			 break;
			 }
		 }
		return;
	}
}