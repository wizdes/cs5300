package coreservlets;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/my-response")
public class MyResponse extends HttpServlet {
	
	List<String> history;
	private ConcurrentMap<String, String> session_info;
	
	 // This Happens Once and is Reused
    public void init(ServletConfig config) throws ServletException
    {
               super.init(config);
               history = new ArrayList<String>();
               session_info = new ConcurrentHashMap<String, String>();
    }
	

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    response.setContentType("text/html");
	    PrintWriter out = response.getWriter();
		Enumeration<String> paramNames = request.getParameterNames();
		String username = "";
	    while(paramNames.hasMoreElements()) {
	    	String paramName = paramNames.nextElement();
	    	String[] paramValues = request.getParameterValues(paramName);
	      
	    	if(paramValues.length == 1 && paramName.equals("Username")){
	    		username = paramValues[0];
	    		if(!session_info.containsKey(username)) session_info.put(username, "");
	    		continue;
    		}
	    	
	    	if (paramValues.length == 1 && paramName.equals("NewText")) {
	    		String paramValue = paramValues[0];
	    		session_info.put(username, paramValue);
	    		continue;
	    	}	    
    	}
	    
	    out.print("<big><big><b>\n"+session_info.get(username)+"</b></big></big>\n");
	    out.print("<html>\n" +
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
	    		"</html>");
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
