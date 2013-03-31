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
		iii) Three servers, local
			1) Replace is replicated properly
		iv) Additional test cases
	b) Beanstalk
4) Extra Credit Options
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

iii) Three servers, local
1) Replace is replicated properly

iv) Additional test cases
1) Confirm that when a server gets a request, it gets added to its group membership
2) When a client gets a cookie, it adds its location metadata to its group membership
3) When a server gets a timeout, the timed out server is removed from the group membership

b) Beanstalk

4) Extra Credit Options

For this project, we have implemented two extra credit features: Get Members, and k-resiliency.

a) Get Members
	This is implemented by having a server, upon receiving a cookie, ask the IPPprimary for its member list with a maximum of 20 members.  
	The IPPprimary server then sends back the member list, and the original server adds those members to its list.
b) k-resiliency
	k-resilience is implemented by having every function that reads from servers in a for loop that walks through the received ip list.  
	The only part where k is used is that the server will designate k-1 backups and send each a sessionWrite().

