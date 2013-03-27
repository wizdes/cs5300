package coreservlets;

import data_layer.SessionData;
import data_layer.sessionKey;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import rpc_layer.ClientStubs;
import rpc_layer.DestinationAddressList;
import rpc_layer.ServerStubs;

@WebServlet("/msgcookieservlet")
public class MsgCookieServlet extends HttpServlet {
	private static final long serialVersionUID = -7173084749627424244L;
	// Used creating cookies to protect session IDs
	private int counter = 0;
	private final String StandardCookieName = "CS5300PROJ1SESSION";
	public final int timeOutSeconds = 60;
	private GarbageCollectionThread garbageCollectionThread;
	private final String DefaultMessage = "Default Message.";
	private String serverID;
	// all data goes through sessionData, although there are no functions in here
	// the objects are public. This should change
	private SessionData myData = null;
	
	//creates a client and server
	private ClientStubs client = null;
	private ServerStubs server = null;
	
	private int k = 1;

	/**
	 * This init is called the first time the servlet is launched
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		// this sets a unique clientID per host
		myData = new SessionData();
		server = new ServerStubs(myData);
		client = new ClientStubs();
		setServerID();
		client.initClient(server.getServerPort());
		
		garbageCollectionThread = new GarbageCollectionThread(myData.sessionState);
		garbageCollectionThread.start();
	}

	/**
	 * This function writes the actual web page out to the browser
	 */
	private void printWebsite(String message, PrintWriter browserPrintWriter,
			String sessionID, HttpServletRequest request, long expiresInSeconds, String foundOnserverID) {
		
		browserPrintWriter.print("<big><big><b>\n" + message
				+ "</b></big></big>\n");
		browserPrintWriter.print("<html>\n" + "<body>\n"
				+ "<form method=GET action=\"msgcookieservlet\">"
				+ "<input type=text name=NewText size=30 maxlength=400>"
				+ "&nbsp;&nbsp;" + "<input type=submit name=cmd value=Replace>"
				+ "&nbsp;&nbsp;" + "</form>"
				+ "<form method=GET action=\"msgcookieservlet\">"
				+ "<input type=submit name=REF value=Refresh>" + "</form>"
				+ "<form method=GET action=\"msgcookieservlet\">"
				+ "<input type=submit name=ESC value=LogOut>" + "</form>"
				+ "</body>" + "</html>");
		browserPrintWriter.print("Session on: " + request.getRemoteAddr() + ":"
				+ request.getRemotePort() + "<br/>");
		browserPrintWriter.print("Server ID: " + serverID + "<br/>");
		if(!message.equals(DefaultMessage)){
			browserPrintWriter.print("Found Data on: " + foundOnserverID + "<br/>");
		}
		browserPrintWriter
				.print("Expires " + new Date(expiresInSeconds * 1000));
		}
	
	public void createAndReplicateNewCookie(HttpServletRequest request,
			PrintWriter browserPrintWriter, HttpServletResponse response) {
		createAndReplicateCookie(request, response, DefaultMessage, null, 0, null);
	}	

	/**
	 * Creates a cookie and replicate it
	 */
	public void createAndReplicateCookie(HttpServletRequest request, HttpServletResponse response,
			String message, String sessionID, int versionNumber, String foundOnserverID) {
		PrintWriter browserPrintWriter = null;
		
		try {
			browserPrintWriter = response.getWriter();
		} catch (IOException e) {}
		
		if(sessionID == null) {
			try {
				// We get the counter, we need to do so with a lock first so that it
				// cannot change from under us
				myData.counterLock.lock();
				sessionID = request.getLocalAddr() + ":"
						+ request.getLocalPort() + Integer.toString(counter++);
			} finally {
				// Unlock the counter
				myData.counterLock.unlock();
			}
		}

		long expirationInSeconds = System.currentTimeMillis() / 1000 + (long) timeOutSeconds;
		// Build the cookie and add it to the response header
		String clientResponseString="";
		int backupServerIndex=-1;
		while(!clientResponseString.contains("Written") && client.getNumServers() > 0){
			//TODO: handle the case where there is no place to write (no available backup)
			
			// expand this for 'k' elements
			backupServerIndex = client.getRandomServerIndex();
			
			byte[] resp = client.sessionWrite(sessionID, Integer.toString(versionNumber), "", "" + expirationInSeconds, backupServerIndex);
			if(resp != null) clientResponseString=new String(resp);
		} 
		
		String locationMetaDataStr = server.getLocationMetaData();
		if(backupServerIndex != -1){
			locationMetaDataStr += ","+client.getDestAddr(backupServerIndex)+":"+client.getDestPort(backupServerIndex);
		}
		
		Cookie retCookie = SessionUtilities.createCookie(StandardCookieName,
				sessionID, versionNumber, locationMetaDataStr);
		response.addCookie(retCookie);
		System.out.println("Making new cookie "+sessionID+","+versionNumber+","+locationMetaDataStr);
		// We grab a lock in order to put the new session into our sessionState
		// map
		myData.createNewSession(sessionID, versionNumber, message, expirationInSeconds);
				
		printWebsite(message, browserPrintWriter, sessionID, request, expirationInSeconds,foundOnserverID);
	}
	
	public void handleBackupServerData(byte[] resp, HttpServletRequest request, PrintWriter out, HttpServletResponse response){
		if(resp == null) {
			// then 'create and replicate'
			createAndReplicateNewCookie(request, out, response);
			return;				
		}
		else {
			// then 'create and replicate'
			String responseString = new String(resp);
			UserContents replicatedResponse = SessionUtilities.parseReplicatedData(responseString);
			createAndReplicateCookie(request, response, replicatedResponse.getMessage(), replicatedResponse.getSessionID(), replicatedResponse.getVersionNumber(),SessionUtilities.getServerID(responseString));
			return;						
		}
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if(request.getCookies()!=null){
			System.out.println("Got this cookie: \n"+SessionUtilities.GetRequestCookie(
							StandardCookieName, request.getCookies()).getValue());
		}
		// Parse the cookie into our custom CookieContents object
		CookieContents cookieContents = SessionUtilities.readCookie(SessionUtilities.GetRequestCookie(
						StandardCookieName, request.getCookies()));
		
		// If the cookie didn't exist create it
		if (cookieContents == null) {
			createAndReplicateNewCookie(request, out, response);
			return;
		}
		
		//let's try to get the data
		sessionKey cookieKey = new sessionKey(cookieContents.getSessionID(), cookieContents.getVersionNumber());
		UserContents session_info = myData.sessionState.get(cookieKey);

		// if it doesn't exist (garbage collected) or has expired, create a new cookie and exit
		if (session_info != null
				&& cookieContents.getVersionNumber() == session_info.getVersionNumber() 
				&& session_info.getExpirationTime() < System.currentTimeMillis() / 1000){
			System.out.println(System.currentTimeMillis()/1000);
			createAndReplicateNewCookie(request, out, response);
			return;
		}
		
		String sessionID = cookieKey.getSessionID();
		
		// we need to make sure we have the latest version; if not, ping the server that does
		if(session_info == null || cookieContents.getVersionNumber() > session_info.getVersionNumber()){
			if(cookieContents.getDestinationAddressList().size()>0){
				System.out.println("lol "+cookieContents.getDestinationAddressList().getDestAddr(0));
				byte[] resp = client.sessionRead(sessionID, Integer.toString(cookieContents.getVersionNumber()), cookieContents.getDestinationAddressList());
				if(resp==null){
					createAndReplicateNewCookie(request, out, response);
					return;
				}
				handleBackupServerData(resp, request, out, response);
			}
			else {
				createAndReplicateNewCookie(request, out, response);
				return;
			}
		}

		// at this point, we have the right data and everything
		// We now process the request
		boolean newCookie = processSession(request, response, cookieKey);
		
		// If we need a new cookie (from a logout click), we go here
		if(newCookie){
			// if it's a logout, then flush it from my own and all the primary/backup from the cookie
			DestinationAddressList dest = new DestinationAddressList();
			SessionUtilities.parseLocationMetadata(cookieContents.getLocationMetadata(), dest);
			client.sessionDelete(sessionID, Integer.toString(cookieContents.getVersionNumber()), dest);

			createAndReplicateNewCookie(request, out, response);
			return;				
		}
		
		System.out.println(sessionID + ":" + myData.sessionState.get(cookieKey).getVersionNumber());
		return;
	}

	/**
	 * Modifies the session state to new information
	 */
	private void modState(String sessionID, int versionNumber, String locationMetadata, HttpServletResponse response,
			HttpServletRequest request, String message) {
		// Set the new cookie and session information
		createAndReplicateCookie(request, response, message, sessionID, versionNumber + 1,"cache");
	}

	/**
	 * Parse the request information, and perform an action
	 */
	private boolean processSession(HttpServletRequest request, HttpServletResponse response, sessionKey cookieKey) {
		// processSession is always called inside a lock, so we don not need to
		String sessionID = cookieKey.getSessionID();
		int versionNum = myData.sessionState.get(cookieKey).getVersionNumber();
		String message = myData.sessionState.get(cookieKey).getMessage();
		Enumeration<String> paramNames = request.getParameterNames();
		
		//this case is when there is nothing in the parameters and it has a session state
		// this is the same behavior as a refresh button click
		if(paramNames.hasMoreElements() == false){
			modState(sessionID, versionNum, "", response, request, message);
			return false;			
		}
		
		// Walk the paramNames list
		while (paramNames.hasMoreElements()) {
			String paramName = paramNames.nextElement();
			String[] paramValues = request.getParameterValues(paramName);

			// Refresh
			if (paramValues.length == 1 && paramName.equals("REF")) {
				createAndReplicateCookie(request, response, message, sessionID, versionNum + 1,"cache");
				return false;
			}

			// Replace
			if (paramValues.length == 1 && paramName.equals("NewText")) {
				String paramValue = paramValues[0].replaceAll("[^(A-Za-z0-9\\.\\-_]","");
				System.out.println(paramValue);
				createAndReplicateCookie(request, response, paramValue, sessionID, versionNum + 1,"cache");
				return false;
			}

			// Logout
			if (paramValues.length == 1 && paramName.equals("ESC")) {
				myData.sessionState.get(cookieKey).setExpirationTime(System.currentTimeMillis() / 1000 - 1);
				return true;
			}
		}
		return false;
	}
	
	private void setServerID(){
		try {
			serverID=InetAddress.getLocalHost().getHostName()+":"+server.getServerPort();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		System.out.println("My serverID "+serverID);

	}
}