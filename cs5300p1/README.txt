Yi Li (yl2326)
Patrick Berens(pcb73)
Max Spector (ms2786)

README

Table of Contents
1) Overall structure of the solution
	a) Format of the cookies
	b) Format of RPC messages
2) Source code
	i) coreservlets
	ii) data_layer
	iii) exceptions
	iv) rpc_layer
3) Testing
	i) Local
	ii) Beanstalk
4) Extra Credit Options
	i) Get Members
	ii) k-resiliency

1) Overall structure of the solution

a) Format of the cookies
	sessionID,versionNumber,locationMetadata
	where locationMetadata=IPPprimary,IPPsecondary
b) Format of RPC messages

2) Source code

a) coreservlets
	This package contains the main serverlet and supporting classes including the 
	garbage collector, cookie contents and session storage classes, and helper classes to do the parsing.
b) data_layer
	This package contains the class that stores the sessions in a map, and a class that generates the keys to look 
	up the session based on the sessionID and versionNumber.
c) exceptions
	This package contains the exception for a packet bigger than 512 bytes.
d) rpc_layer
	This package contains all of the rpc work.  It contains an rpc client class which will connect to an rpc server to perform operations.  
	It also contains an rpc server class which will listen for connections and perform the operation.  It contains a helper class that holds 
	lists of addresses and ports, a helper class that marshalls and unmarhalls data to be sent on the socket, and the operations enum class.
3) Testing

a) Local

b) Beanstalk

4) Extra Credit Options

a) Get Members
	This is implemented by having a server, upon receiving a cookie, ask the IPPprimary for its member list with a maximum of 20 members.  
	The IPPprimary server then sends back the member list, and the original server adds those members to its list.
b) k-resiliency
	k-resilience is implemented by having every function that reads from servers in a for loop that walks through the received ip list.  
	The only part where k is used is that the server will designate k-1 backups and send each a sessionWrite().

