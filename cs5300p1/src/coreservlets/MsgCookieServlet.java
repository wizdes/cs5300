package coreservlets;

import data_layer.SessionData;
import java.io.*;
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
		client.initClient(server.getServerPort());
		myData = new SessionData();
		client = new ClientStubs();
		server = new ServerStubs(myData);
		garbageCollectionThread = new GarbageCollectionThread(myData.sessionState,
				myData.sessionLocks);
		garbageCollectionThread.start();
	}

	/**
	 * This function writes the actual web page out to the browser
	 */
	private void printWebsite(String message, PrintWriter browserPrintWriter,
			String sessionID, HttpServletRequest request, long expiresInSeconds) {
		
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
		browserPrintWriter
				.print("Expires " + new Date(expiresInSeconds * 1000));
		}
	
	public void createAndReplicateNewCookie(HttpServletRequest request,
			PrintWriter browserPrintWriter, HttpServletResponse response) {
		createAndReplicateCookie(request, response, DefaultMessage, null, 0);
	}	

	/**
	 * Creates a cookie and replicate it
	 */
	public void createAndReplicateCookie(HttpServletRequest request, HttpServletResponse response,
			String message, String sessionID, int versionNumber) {
		PrintWriter browserPrintWriter = null;
		
		try {
			browserPrintWriter = response.getWriter();
		} catch (IOException e) {}
		
		if(sessionID == null) {
			try {
				// We get the counter, we need to do so with a lock first so that it
				// cannot change from under us
				myData.counterLock.lock();
				sessionID = "Servername" + Integer.toString(counter++);
			} finally {
				// Unlock the counter
				myData.counterLock.unlock();
			}
		}

		long expirationInSeconds = System.currentTimeMillis() / 1000 + (long) timeOutSeconds;
		// Build the cookie and add it to the response header
		String clientResponseString="";
		int backupServerIndex=-1;
		while(!clientResponseString.contains("Written")){
			//TODO: handle the case where there is no place to write (no available backup)
			
			// expand this for 'k' elements
			backupServerIndex = client.getRandomServerIndex();
			
			byte[] resp = client.sessionWrite(sessionID, Integer.toString(versionNumber), "", "" + expirationInSeconds, backupServerIndex);

			if(resp != null) clientResponseString=new String(resp);
		} 
		Cookie retCookie = SessionUtilities.createCookie(StandardCookieName,
				sessionID, versionNumber, server.getLocationMetaData()+","+client.getDestAddr(backupServerIndex)+":"+client.getDestPort(backupServerIndex));
		response.addCookie(retCookie);

		// We grab a lock in order to put the new session into our sessionState
		// map
		myData.createNewSession(sessionID, versionNumber, message, expirationInSeconds);
				
		printWebsite(message, browserPrintWriter, sessionID, request, expirationInSeconds);
	}
	
	private DestinationAddressList getMetadataLocations(String metaDatLocations){
		
		return new DestinationAddressList();
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
			createAndReplicateCookie(request, response, replicatedResponse.getMessage(), replicatedResponse.getSessionID(), replicatedResponse.getVersionNumber());
			return;						
		}
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		// Parse the cookie into our custom CookieContents object
		CookieContents cookieContents = SessionUtilities
				.readCookie(SessionUtilities.GetRequestCookie(
						StandardCookieName, request.getCookies()));
		
		// If the cookie didn't exist create it
		if (cookieContents == null) {
			createAndReplicateNewCookie(request, out, response);
			return;
		}
		
		// If it did exist but we do not know about it, create a new cookie
		// Note: this happens usually when the user has a cookie which has been
		// garbage collected
		String sessionID = cookieContents.getSessionID();
		ReentrantLock SessionLock = myData.sessionLocks.get(sessionID);
		if (SessionLock == null) {
			// check if it exists in the latest location 
			// if it's there, then do a sessionRead
			DestinationAddressList dest = new DestinationAddressList();
			SessionUtilities.parseLocationMetadata(cookieContents.getLocationMetadata(), dest);
			byte[] resp = client.sessionRead(sessionID, Integer.toString(cookieContents.getVersionNumber()), dest);

			handleBackupServerData(resp, request, out, response);
			return;
		}
		try {
			// If a lock exists, we grab it
			SessionLock.lock();
			UserContents session_info = myData.sessionState.get(sessionID);

			// we would have to check to make sure the session data is valid
			// this is an issue if we have an old session data, but we think it's expired
			// I think we should get rid of locks
			// If we do not have session data, or it has expired and it is the correct version, we create a new cookie
			if (session_info == null
					|| (cookieContents.getVersionNumber() == session_info.getVersionNumber() 
					&& session_info.getExpirationTime() < System.currentTimeMillis() / 1000)) {
				createAndReplicateNewCookie(request, out, response);
				return;
			}
			
			if(cookieContents.getVersionNumber() > session_info.getVersionNumber()){
				DestinationAddressList dest = new DestinationAddressList();
				SessionUtilities.parseLocationMetadata(cookieContents.getLocationMetadata(), dest);
				byte[] resp = client.sessionRead(sessionID, Integer.toString(cookieContents.getVersionNumber()), dest);
				handleBackupServerData(resp, request, out, response);
			}

			// otherwise keep going
			// We now process the request
			boolean newCookie = processSession(request, sessionID, response);
			
			// If we need a new cookie (from a logout click), we go here
			if(newCookie){
				// "HANDLE AND REPLICATE THE DATA PROPERLY"
				// if it's a logout, then flush it from my own and all the primary/backup from the cookie
				DestinationAddressList dest = new DestinationAddressList();
				SessionUtilities.parseLocationMetadata(cookieContents.getLocationMetadata(), dest);
				client.sessionDelete(sessionID, Integer.toString(cookieContents.getVersionNumber()), dest);

				createAndReplicateNewCookie(request, out, response);
				return;				
			}
			
			System.out.println(sessionID + ":" + myData.sessionState.get(sessionID).getVersionNumber());

			// Print the page out to the browser
			printWebsite(myData.sessionState.get(sessionID).getMessage(), out, sessionID, request, 
					myData.sessionState.get(sessionID).getExpirationTime());
		} finally {
			// Unlock the session
			SessionLock.unlock();
		}
		return;
	}

	/**
	 * Modifies the session state to new information
	 */
	private void modState(String sessionID, int versionNumber, String locationMetadata, HttpServletResponse response,
			HttpServletRequest request, String message) {
		// Set the new cookie and session information
		createAndReplicateCookie(request, response, message, sessionID, versionNumber + 1);
	}

	/**
	 * Parse the request information, and perform an action
	 */
	private boolean processSession(HttpServletRequest request, String sessionID,
			HttpServletResponse response) {
		// processSession is always called inside a lock, so we don not need to
		// lock
		int versionNum = myData.sessionState.get(sessionID).getVersionNumber();
		String message = myData.sessionState.get(sessionID).getMessage();
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
				modState(sessionID, versionNum, "", response, request, message);
				return false;
			}

			// Set new text
			if (paramValues.length == 1 && paramName.equals("NewText")) {
				String paramValue = paramValues[0].replaceAll("[^(A-Za-z0-9\\.\\-_]","");
				System.out.println(paramValue);
				modState(sessionID, versionNum, "", response, request, paramValue);
				return false;
			}

			// Logout
			if (paramValues.length == 1 && paramName.equals("ESC")) {
				// Again this function is always called in a lock, so we don't
				// need a new one
				myData.sessionState.get(sessionID).setExpirationTime(
						System.currentTimeMillis() / 1000 - 1);
				return true;
			}
		}
		return false;
	}
}