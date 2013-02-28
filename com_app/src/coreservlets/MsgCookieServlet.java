package coreservlets;

import java.io.*;
import java.util.Date;
import java.util.Enumeration;
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
	private static final long serialVersionUID = -7173084749627424244L;
	private final ReentrantLock counterLock = new ReentrantLock(true); // Used
																		// when
																		// creating
																		// cookies
																		// to
																		// protect
																		// session
																		// id,
																		// etc
	private final ConcurrentMap<Integer, ReentrantLock> sessionLocks = new ConcurrentHashMap<>(); // Locks
																									// for
																									// a
																									// given
																									// user/session
	private final ConcurrentMap<Integer, UserContents> sessionState = new ConcurrentHashMap<>(); // User
																									// contents
	private int counter = 0;
	private final String StandardCookieName = "CS5300PROJ1SESSION";
	public final int timeOutSeconds = 5;
	private GarbageCollectionThread garbageCollectionThread;
	private final String DefaultMessage = "Default Message.";

	/**
	 * This init is called the first time the servlet is launched
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		garbageCollectionThread = new GarbageCollectionThread(sessionState,
				sessionLocks);
		garbageCollectionThread.start();
	}

	/**
	 * This function writes the actual webpage out to the browser
	 * 
	 * @param message
	 *            The message to send to the user
	 * @param browserPrintWriter
	 *            The PrintWriter that will write to the brower socket
	 * @param sessionID
	 *            The session ID of the user
	 * @param request
	 *            The request that resulted in this page
	 * @param expiresInSeconds
	 *            The expiration time of the cookie
	 */
	private void printWebsite(String message, PrintWriter browserPrintWriter,
			int sessionID, HttpServletRequest request, long expiresInSeconds) {
		browserPrintWriter.print("<big><big><b>\n" + message
				+ "</b></big></big>\n");
		browserPrintWriter.print("<html>\n" + "<body>\n"
				+ "<form method=GET action=\"msgcookieservlet\">"
				+ "<input type=text name=NewText size=30 maxlength=512>"
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

	/**
	 * Creates a cookie and
	 * 
	 * @param request
	 *            The request that caused this cookie
	 * @param browserPrintWriter
	 *            The PrintWriter on the brower socket
	 * @param response
	 *            The object that controls the response headers
	 */
	public void createCookie(HttpServletRequest request,
			PrintWriter browserPrintWriter, HttpServletResponse response) {
		int sessionID = -1; // Invalid session ID
		try {
			// We get the counter, we need to do so with a lock first so that it
			// cannot change from under us
			counterLock.lock();
			sessionID = counter++;
		} finally {
			// Unlock the counter
			counterLock.unlock();
		}

		long expiration_date = System.currentTimeMillis() / 1000
				+ (long) timeOutSeconds;
		UserContents insertNew = new UserContents(sessionID, 0, DefaultMessage,
				expiration_date);
		// Build the cookie and add it to the response header
		Cookie retCookie = SessionUtilities.createCookie(StandardCookieName,
				sessionID, 0, "");
		response.addCookie(retCookie);

		// We grab a lock in order to put the new session into our sessionState
		// map
		final ReentrantLock insertLock = new ReentrantLock();
		try {
			insertLock.lock();
			sessionLocks.put(sessionID, insertLock);
			sessionState.put(sessionID, insertNew);
			printWebsite(DefaultMessage, browserPrintWriter, sessionID,
					request, expiration_date);
		} finally {
			// We unlock the lock
			insertLock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
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
			createCookie(request, out, response);
			return;
		}

		// If it did exist but we do not know about it, create a new cookie
		// Note: this happens usually when the user has a cookie which has been
		// garbage collected
		int sessionID = cookieContents.getSessionID();
		ReentrantLock SessionLock = sessionLocks.get(sessionID);
		if (SessionLock == null) {
			createCookie(request, out, response);
			return;
		}
		try {
			// If a lock exists, we grab it
			SessionLock.lock();
			UserContents session_info = sessionState.get(sessionID);

			// If we do not have session data, or it has expired, we create a
			// new cookie
			if (session_info == null
					|| session_info.getExpirationTime() < System
							.currentTimeMillis() / 1000) {
				createCookie(request, out, response);
				return;
			}

			// We now process the request
			processSession(request, sessionID, response);

			// Print the page out to the browser
			printWebsite(sessionState.get(sessionID).getMessage(), out,
					sessionID, request, sessionState.get(sessionID)
							.getExpirationTime());
		} finally {
			// Unlock the session
			SessionLock.unlock();
		}
		return;
	}

	/**
	 * Modifies the session state to new information
	 * 
	 * @param sessionID
	 *            The session ID to set
	 * @param versionNumber
	 *            The version number to set
	 * @param locationMetadata
	 *            The locationMetadata to set
	 * @param response
	 *            The response header to add the cookie to
	 * @param message
	 *            The message to add to the UserContents object
	 */
	private void modState(int sessionID, int versionNumber,
			String locationMetadata, HttpServletResponse response,
			String message) {
		// Set the new cookie and session information
		response.addCookie(SessionUtilities.createCookie(StandardCookieName,
				sessionID, versionNumber + 1, locationMetadata));
		UserContents userContents = new UserContents(sessionID,
				versionNumber + 1, new String(message),
				System.currentTimeMillis() / 1000 + (long) timeOutSeconds);
		// We don't need to lock here because we always call it in a lock
		sessionState.put(sessionID, userContents);
	}

	/**
	 * Parse the request information, and perform an action
	 * 
	 * @param request
	 *            The request to parse
	 * @param sessionID
	 *            The session ID of the current session
	 * @param response
	 *            The response to add headers to
	 */
	private void processSession(HttpServletRequest request, int sessionID,
			HttpServletResponse response) {
		// processSession is always called inside a lock, so we don not need to
		// lock
		int versionNum = sessionState.get(sessionID).getVersionNumber();
		String message = sessionState.get(sessionID).getMessage();
		Enumeration<String> paramNames = request.getParameterNames();
		// Walk the paramNames list
		while (paramNames.hasMoreElements()) {
			String paramName = paramNames.nextElement();
			String[] paramValues = request.getParameterValues(paramName);

			// Refresh
			if (paramValues.length == 1 && paramName.equals("REF")) {
				modState(sessionID, versionNum, "", response, message);
				break;
			}

			// Set new text
			if (paramValues.length == 1 && paramName.equals("NewText")) {
				String paramValue = paramValues[0];
				System.out.println(paramValue);
				modState(sessionID, versionNum, "", response, paramValue);
				break;
			}

			// Logout
			if (paramValues.length == 1 && paramName.equals("ESC")) {
				// Again this function is always called in a lock, so we don't
				// need a new one
				sessionState.get(sessionID).setExpirationTime(
						System.currentTimeMillis() / 1000 - 1);
				break;
			}
		}
		return;
	}
}