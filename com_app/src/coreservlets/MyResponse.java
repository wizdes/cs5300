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
	private String StandardCookieName = "CS5300PROJ1SESSION";
	public int time_out_secs = 5;
	
	private class DataContents{
		public DataContents(int sessionID, int version_number) {
			this.sessionID = sessionID;
			this.version_number = version_number;
		}
		public int sessionID;
		public int version_number;
	}
	
	private class CookieContents extends DataContents{
		public CookieContents(int sessionID, int version_number, String lm){
			super(sessionID, version_number);
			this.lm =  lm;
		}
		private String lm;
	}
	
	private class UserContents extends DataContents{
		public UserContents(int sessionID, int version_number, String message, long time_in_secs){
			super(sessionID, version_number);
			this.message = message;
			this.time_in_secs = time_in_secs;
		}
		private String message = new String();
		private long time_in_secs = 0;
	}
	
	Cookie createCookie(int sessionID, int version_number, String lm){

		Cookie retCookie =  new Cookie(StandardCookieName, sessionID + "," + version_number + "," + lm);
		return retCookie;
	}
	
	CookieContents readCookie(Cookie c) {
		if(c == null) return null;
		String[] contents = c.getValue().split(",");
		CookieContents read = new CookieContents(Integer.parseInt(contents[0]),Integer.parseInt(contents[1]), "");
		read.lm = (contents.length > 2) ? contents[2] : "";
		return read;
	}
	
	 // This Happens Once and is Reused
    public void init(ServletConfig config) throws ServletException
    {
    	super.init(config); 
    }
    
    Cookie GetRequestCookie(Cookie[] cookies){
    	if(cookies == null) return null;
    	for(Cookie c:cookies){
    		if(c.getName().equals(StandardCookieName)){
    			return c;
    		}
    	}
    	return null;
    }
    
    public synchronized Cookie modCounterCreateCookie(int version_number, String msg, String lm){
    	Cookie retCookie = createCookie(counter, version_number, lm);
    	UserContents uc = new UserContents(counter, version_number, new String(msg), System.currentTimeMillis()/1000 + (long)time_out_secs );
    	session_info.put(counter, uc);
    	counter += 1;
    	return retCookie;
    }
    
    public void modState(CookieContents cc, HttpServletResponse response, String msg){
		response.addCookie(createCookie(cc.sessionID, cc.version_number + 1, cc.lm));
    	UserContents uc = new UserContents(cc.sessionID, cc.version_number, new String(msg), System.currentTimeMillis()/1000 + (long)time_out_secs);
    	session_info.put(cc.sessionID, uc);
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
	    CookieContents cc = readCookie(GetRequestCookie(request.getCookies()));
		if(cc == null || session_info.get(cc.sessionID) == null){
			Cookie resp_cookie = modCounterCreateCookie(0, "Default Message.", "");
		    response.addCookie(resp_cookie);
		    printWebsite("Default Message.", out, cc);
		    return;
		}
		
		Enumeration<String> paramNames = request.getParameterNames();
	    while(paramNames.hasMoreElements()) {
	    	String paramName = paramNames.nextElement();
	    	String[] paramValues = request.getParameterValues(paramName);
	    	
	    	if (paramValues.length == 1 && paramName.equals("REF")){
	    		modState(cc, response, session_info.get(cc.sessionID).message);
	    		break;
	    	}
	    	
	    	if (paramValues.length == 1 && paramName.equals("NewText")) {
	    		String paramValue = paramValues[0];
	    		modState(cc, response, paramValue);
	    		break;
	    	}	    
	    	
	    	if (paramValues.length == 1 && paramName.equals("ESC")){
	    		printWebsite("Default Message.", out, cc);
	    		session_info.remove(cc.sessionID);
	    		return;
	    	}
    	}
    
	    printWebsite(session_info.get(cc.sessionID).message, out, cc);

	    synchronized(this){		    
	    for(Iterator<ConcurrentMap.Entry<Integer, UserContents>> it 
	    		= session_info.entrySet().iterator();
	    		it.hasNext();){
	    	ConcurrentMap.Entry<Integer, UserContents> e = it.next();
		    	if(((UserContents)e.getValue()).time_in_secs < System.currentTimeMillis()/1000){
		    		it.remove();
		    	}
	    	}
	    }	    
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
