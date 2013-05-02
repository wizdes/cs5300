Yi Li (yl2326)
Patrick Berens(pcb73)
Max Spector (ms2786)

README

Table of Contents
1) Overall structure of the solution
	a) Format of the cookies
	b) Format of RPC messages
		i) Client requests
		ii) Server responses
2) Source code
	a) coreservlets
	b) data_layer
	c) exceptions
	d) rpc_layer
3) Testing
	a) Local
		i) One server, local
			1) Replace functionality
			2) Refresh functionality
			3) Logout functionality
			4) Crash functionality
		ii) Two servers, local
			1) Replace is replicated
			2) Refresh functionality across servers
			3) Logout functionality across servers
			4) Crash
		iii) Additional test cases
	b) Beanstalk
4) Design decisions
5) Extra Credit Options
	a) Get Members
	b) k-resiliency

1) Overall structure of the solution

a) Format of the cookies
	The format of the cookies is:
	<sessionID,versionNumber,locationMetadata>
	where locationMetadata=IPP_1,IPP_2... IPP_(k+1) in a k-resilient system

	For an example, in a 1-resilient system, an cookie may look like:
	192.168.1.101:57864:0,1,192.168.1.101:57864,192.168.1.101:57865

	where session ID = 192.168.1.101:57864:0
	versionNumber = 1
	locationMetadata = 192.168.1.101:57864,192.168.1.101:57865

b) Format of RPC messages

The format of RPC messages depend on the type of message sent. 

i) Client Requests

In the following requests, callID refers to the a unique ID for the call.

- SessionRead()
	These requests have the following format: <callID, 	operation code, sessionID, verionNumber, and server port>.
- SessionWrite()
	These requests have the following format: <callID, 	operation code, sessionID, verionNumber, data, discard time, and server port>.
- SessionDelete()
	These requests have the following format: <callID, 	operation code, sessionID, verionNumber, and server port>.
- getMembers()
	These requests have the following format: <callID, 	operation code, size of members requested, and server port>.
2) Source code

We structured our code into serveral layers to handle the different types of requests and follow good Java style using abstraction.

a) coreservlets
	This package contains the main serverlet and supporting classes including the garbage collector, cookie contents and session storage classes, and helper classes to do the parsing.
b) data_layer
	This package contains the class that stores the sessions in a map, and a class that generates the keys to look up the session based on the sessionID and versionNumber.
c) exceptions
	This package contains the exception for a packet bigger than 512 bytes.
d) rpc_layer
	This package contains all of the rpc work.  It contains an rpc client class which will connect to an rpc server to perform operations.  
	It also contains an rpc server class which will listen for connections and perform the operation.  It contains a helper class that holds lists of addresses and ports, a helper class that marshalls and unmarhalls data to be sent on the socket, and the operations enum class.
3) Testing

To gain assurance of our system, we tested on both local machines as well as on Beanstalk.

a) Local

We tested our program locally with several different configurations aimed at looking at several different functionalities.

i) One server, local

1) Replace functionality
First start the server. Next type a message in the input field and press the replace button. The page should give a new expiration and the inputted string.
2) Refresh functionality
First start the server. Next type a message in the input field and press the replace button. The page should give a new expiration and the inputted string. Next, press refresh. A new expiration should've appeared.
3) Logout functionality
First start the server. Next type a message in the input field and press the replace button. The page should give a new expiration and the inputted string. Press the Logout button. You should now see the default message.
4) Crash functionality
Press the crash button. You should no longer be able to access the servlet.

ii) Two servers, local

1) Replace is replicated
On server A, enter a message into the field and press 'Replace'. On server B, access the servlet and see your message on that server. The data should show it is on both servers.
2) Refresh functionality across servers
On server A, enter a message into the field and press 'Replace'. On server B, access the servlet and see your message on that server. The data should show it is on both servers. On server B, replace the message with a new message. Next, on server A, press 'Refresh'. You should see the new message on server A. 
3) Logout functionality across servers
On server A, enter a message into the field and press 'Replace'. On server B, access the servlet and see your message on that server. The data should show it is on both servers. On server B, press 'Logout'. On server A, you should see the default message.
4) Crash
On server A, enter a message into the field and press 'Replace'. On server B, access the servlet and see your message on that server. The data should show it is on both servers. Now press 'Crash' on server B. Server A should still have your session. 

iv) Additional test cases
1) Confirm that when a server gets a request, it gets added to its group membership
2) When a client gets a cookie, it adds its location metadata to its group membership
3) When a server gets a timeout, the timed out server is removed from the group membership
4) Session timeouts remove the session from being accessed
5) Testing for a type 3 error by modifying the cookie to be an older version (this assumes the cookie sent from the server did not reach the client). This should still contain the same data. In our system, we assume a 1-resilient system as the version is handled when it tries to find the cookie on another server.

b) Beanstalk
Our Elastic Beanstalk setup procedure:
1) Create a new environment 
2) setting the container type and uploading our .war file
3) When the Environment is created, go into 'Edit Configuration'. Here, set the minimum number of instances to 3. 
4) Next, modify the Security Group (in the EC2 console) to have all inbound UDP connections on ports 0-65535 accessible from source 192.168.2.0/24.
5) Test the Beanstalk instance of our code.

It is important to note that 'crashing' a server on Beanstalk will produce some unexpected failure on the Load Balancer. This is because the Load Balancer is unable to detect that a server has failed immediately after the failure and may still redirect some requests to that server. Since we cannot configure the Load Balancer, one can expect to wait a short amount of time to continue normal operations after a crash.

Another issue with Beanstalk is the fact that the Load Balancer tries to send requests to the same server. This is an issue since if the client only talks to one server, the server does not get the opportunity to talk to other servers; this is like the situation with newly-booted servers. To resolve this, a client can create a session and refresh until the displayed memberSet has more elements. 

4) Design decisions

We chose delta = 5000 milliseconds so it represents the maximum allowable difference between any pair of client and server clocks plus the maximum allowable clock drift over a session timeout interval.
We also chose gamma = 50 milliseconds accounting for the communication and processing time associated with the SessionWrite call. 

5) Extra Credit Options

For this project, we have implemented two extra credit features: Get Members, and k-resiliency.

a) Get Members
	This is implemented by having a server, upon receiving a cookie, ask the IPPprimary for its member list with a maximum of 20 members.  
	The IPPprimary server then sends back the member list, and the original server adds those members to its list.
b) k-resiliency
	k-resilience is implemented by having every function that reads from servers in a for loop that walks through the received ip list.  
	The only part where k is used is that the server will designate k-1 backups and send each a sessionWrite(). In the Java class 'Contants.java', a user can change the resiliency of the system by modifying the variable 'k'.

