package rpc_layer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import coreservlets.UserContents;

import data_layer.SessionData;
import data_layer.sessionKey;
import rpc_layer.Marshalling;

public class ServerStubs extends Thread{
	private static DatagramSocket rpcSocket = null;
	private SessionData myData = null;
	private DestinationAddressList clientAddresses;
	public static final int MAX_UDP_PKT_SIZE = 512;	//Bytes

	//returns the port number to be used on client stubs
	public ServerStubs(SessionData data){
		myData = data;
		try {
			rpcSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public void addServerList(DestinationAddressList clientAddresses){
		this.clientAddresses = clientAddresses;
	}
	
	public static int getServerPort(){
		return rpcSocket.getLocalPort();
	}
	public String getLocationMetaData(){
		try {
			return InetAddress.getLocalHost().getHostAddress()+":"+rpcSocket.getLocalPort();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}
	public void run(){
		while(true){
			try {
				byte[] inBuf = new byte[MAX_UDP_PKT_SIZE];
				DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
				rpcSocket.receive(recvPkt);
				InetAddress returnAddr = recvPkt.getAddress();
				int returnPort = recvPkt.getPort();
				System.out.println("Got "+new String(inBuf));
				Object[] elements = Marshalling.unmarshall(inBuf);
				int returnServerPort = Integer.parseInt((String)elements[elements.length - 1]);
				clientAddresses.addDestAddress(returnAddr, returnServerPort);
				OperationEnums operationCode = OperationEnums.valueOf((String) elements[1]);
				
				String[] outBufStr = null;
				switch(operationCode){
					case operationSESSIONREAD:
						outBufStr = sessionRead((String)elements[2], (String)elements[3]);
						break;
					case operationSESSIONWRITE:
						outBufStr = sessionWrite((String)elements[2], (String)elements[3], (String)elements[4], (String)elements[5]);
						break;
					case operationDELETE:
						outBufStr = sessionDelete((String)elements[2], (String)elements[3]);
						break;
					case operationGETMEMBERS:
						outBufStr = getMembers(Integer.parseInt((String)elements[2]));
						break;
					default:
						break;						
				}
				String[] tempList = new String[outBufStr.length + 2];
				tempList[0] = (String) elements[0];
				tempList[1] = InetAddress.getLocalHost().getHostAddress()+":"+rpcSocket.getLocalPort();
				//tempList[1] = 
				System.arraycopy(outBufStr, 0, tempList, 2, outBufStr.length);
				
				System.out.println("Server sending to Client Stub.");
				System.out.println(Arrays.toString(tempList));
				
				byte[] outBuf = Marshalling.marshall(tempList);
				
				DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, returnAddr, returnPort);
				rpcSocket.send(sendPkt);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public String[] constructNotFoundResponse(){
		String[] notFoundResp = new String[1];
		notFoundResp[0] = "Not found";
		return notFoundResp;	
	}


	public String[] sessionRead(String SID, String version) {
		UserContents session_info = myData.sessionState.get(new sessionKey(SID, Integer.parseInt(version)));

		if(session_info == null){
			//let's look a little deeper
			sessionKey attemptAgain = null;
			int highestVersion = 0;
			for (sessionKey o : myData.sessionState.keySet()) {
			    // ...
				if(o.getSessionID().equals(SID) && o.getVersionNumber() >= highestVersion){
					attemptAgain = o;
					highestVersion = o.getVersionNumber();
				}
			}
			if(highestVersion > Integer.parseInt(version)) session_info = myData.sessionState.get(attemptAgain);
			else session_info = null;
			
		}
		if(session_info == null){
			return constructNotFoundResponse();
		}
		else{
			String[] foundResp = new String[2];
			foundResp[0] = Integer.toString(session_info.getVersionNumber());
			foundResp[1] = session_info.getMessage();
			return foundResp;
		}
	}

	public String[] sessionWrite(String SID, String version, String data, String discardTime) {
		UserContents session_info = myData.sessionState.get(new sessionKey(SID, Integer.parseInt(version)));
		
		if(session_info == null){
			//create a new one				
			myData.createNewSession(SID, Integer.parseInt(version), data, Long.parseLong(discardTime));
		}
		else{
			session_info.setMessage(data);
			session_info.setExpirationTime(Long.parseLong(discardTime));
		}
		String[] writtenResp = new String[1];
		writtenResp[0] = "Written";
		return writtenResp;		
	}

	public String[] sessionDelete(String SID, String version) {
		UserContents session_info = myData.sessionState.get(new sessionKey(SID, Integer.parseInt(version)));
		if(session_info == null){
			return constructNotFoundResponse();
		}
		else{
			myData.sessionState.remove(new sessionKey(SID, Integer.parseInt(version)));
			String[] writtenResp = new String[1];
			writtenResp[0] = "Deleted";
			return writtenResp;
		}
	}

	public String[] getMembers(int sz) {
		String [] members= new String[clientAddresses.size() < sz ? clientAddresses.size() : sz];
		for (int i=0; i<(clientAddresses.size() < sz ? clientAddresses.size() : sz); i++){
			members[i]=clientAddresses.getDestAddr(i)+":"+clientAddresses.getDestPort(i);
		}
		return members;
	}

}
