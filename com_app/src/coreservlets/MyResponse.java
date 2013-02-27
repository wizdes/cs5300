package coreservlets;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/my-response")
public class MyResponse extends HttpServlet {
	
	private ConcurrentMap<Integer, UserContents> session_info = new ConcurrentHashMap<Integer, UserContents>();
	private int counter = 0;
	private final String StandardCookieName = "CS5300PROJ1SESSION";
	public final int time_out_secs = 5;
	private GarbageCollectionThread garbageCollectionThread;
	 // This Happens Once and is Reused
    public void init(ServletConfig config) throws ServletException
    {
    	super.init(config); 
    	garbageCollectionThread = new GarbageCollectionThread(session_info);
    	garbageCollectionThread.start();
    	
    }
    

    
    public synchronized Cookie modCounterCreateCookie(int versionNumber, String message, String lm){
    	Cookie retCookie = SessionUtilities.createCookie(StandardCookieName, counter, versionNumber, lm);
    	UserContents userContents = new UserContents(counter, versionNumber, new String(message), System.currentTimeMillis()/1000 + (long)time_out_secs );
    	session_info.put(counter, userContents);
    	counter += 1;
    	return retCookie;
    }
    
    public void modState(CookieContents cookieContents, HttpServletResponse response, String msg){
		response.addCookie(SessionUtilities.createCookie(StandardCookieName, cookieContents.getSessionID(), cookieContents.getVersionNumber() + 1, cookieContents.getLm()));
    	UserContents uc = new UserContents(cookieContents.getSessionID(), cookieContents.getVersionNumber(), new String(msg), System.currentTimeMillis()/1000 + (long)time_out_secs);
    	session_info.put(cookieContents.getSessionID(), uc);
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
	

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    response.setContentType("text/html");
	    PrintWriter out = response.getWriter();
		
		// first time cookie and website handling here
	    CookieContents cookieContents = SessionUtilities.readCookie(SessionUtilities.GetRequestCookie(StandardCookieName,request.getCookies()));
		if(cookieContents == null || session_info.get(cookieContents.getSessionID()) == null){
			Cookie resp_cookie = modCounterCreateCookie(0, "Default Message.", "");
		    response.addCookie(resp_cookie);
		    printWebsite("Default Message.", out, cookieContents);
		    return;
		}
		
		Enumeration<String> paramNames = request.getParameterNames();
	    while(paramNames.hasMoreElements()) {
	    	String paramName = paramNames.nextElement();
	    	String[] paramValues = request.getParameterValues(paramName);
	    	
	    	if (paramValues.length == 1 && paramName.equals("REF")){
	    		modState(cookieContents, response, session_info.get(cookieContents.getSessionID()).getMessage());
	    		break;
	    	}
	    	
	    	if (paramValues.length == 1 && paramName.equals("NewText")) {
	    		String paramValue = paramValues[0];
	    		modState(cookieContents, response, paramValue);
	    		break;
	    	}	    
	    	
	    	if (paramValues.length == 1 && paramName.equals("ESC")){
	    		printWebsite("Default Message.", out, cookieContents);
	    		session_info.remove(cookieContents.getSessionID());
	    		return;
	    	}
    	}
    
	    printWebsite(session_info.get(cookieContents.getSessionID()).getMessage(), out, cookieContents);
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
