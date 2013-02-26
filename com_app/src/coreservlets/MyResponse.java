package coreservlets;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
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
	
	private class DataContents{
		public int sessionID;
		public int version_number;
	}
	
	private class CookieContents extends DataContents{
		private String lm;
	}
	
	private class UserContents extends DataContents{
		private String message;
		private int time_in_secs;
	}
	
	Cookie createCookie(int sessionID, int version_number, String lm){

		Cookie retCookie =  new Cookie(StandardCookieName, sessionID + "," + version_number + "," + lm);
		
		return retCookie;
	}
	
	CookieContents readCookie(Cookie c) {
		CookieContents read = new CookieContents();
		String[] contents = c.getValue().split(",");
		read.sessionID = Integer.parseInt(contents[0]);
		read.version_number = Integer.parseInt(contents[1]);
		read.lm = (contents.length > 2) ? contents[2] : "";
		return read;
	}
	
	 // This Happens Once and is Reused
    public void init(ServletConfig config) throws ServletException
    {
    	super.init(config); 
    }
    
    Cookie GetRequestCookie(Cookie[] cookies){
    	for(Cookie c:cookies){
    		if(c.getName().equals(StandardCookieName)){
    			return c;
    		}
    	}
    	return null;
    }
    
    public synchronized Cookie modCounterGetCookie(int version_number, String lm){
    	Cookie retCookie = createCookie(counter, version_number, lm);
    	counter += 1;
    	return retCookie;
    }
	

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    response.setContentType("text/html");
	    PrintWriter out = response.getWriter();

		
		// cookie stuff here
		Cookie req_cookie = GetRequestCookie(request.getCookies());
		if(req_cookie == null){
			modCounterGetCookie(0, "");
		    out.print("<big><big><b>\n"+session_info.get(0)+"</b></big></big>\n");
		    out.print(getWebsite());
		    return;
		}
		
		// response stuff
		
		Enumeration<String> paramNames = request.getParameterNames();
		String username = "";
	    while(paramNames.hasMoreElements()) {
	    	String paramName = paramNames.nextElement();
	    	String[] paramValues = request.getParameterValues(paramName);
	    	
	    	if (paramValues.length == 1 && paramName.equals("NewText")) {
	    		String paramValue = paramValues[0];
	    		//session_info.put(username, paramValue);
	    		continue;
	    	}	    
    	}
	    
	    out.print("<big><big><b>\n"+session_info.get(0)+"</b></big></big>\n");
	    out.print(getWebsite());
	}
	
	String getWebsite(){
		return "<html>\n" +
    			"<body>\n" +
    			"<form method=GET action=\"my-response\">" +
    			"<input type=text name=Username size=10 maxlength=512>" +
				"&nbsp;&nbsp;" +
				"<input type=text name=NewText size=30 maxlength=512>" +
				"&nbsp;&nbsp;" +
				"<input type=submit name=cmd value=Save>" +
				"&nbsp;&nbsp;" +
				"</form>" +
				"<form method=GET action=\"my-response\">" +
				"<input type=text name=Username size=10 maxlength=512>" +
				"&nbsp;&nbsp;" +
				"<input type=submit name=cmd value=Refresh>" +
				"</form>" +
				"<form method=GET action=\"eg3.html\">" +
				"<input type=submit name=cmd value=LogOut>" +
				"</form>" +
				"</body>" +
	    		"</html>";
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
