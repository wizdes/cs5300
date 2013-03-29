package rpc_layer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.concurrent.locks.ReentrantLock;

import network_layer.UDPNetwork;

import coreservlets.UserContents;

import data_layer.SessionData;
import data_layer.sessionKey;
import rpc_layer.Marshalling;

public class ServerStubs extends Thread{
	DatagramSocket rpcSocket = null;
	SessionData myData = null;
	private DestinationAddressList clientAddresses;
	
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
	
	public int getServerPort(){
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
				byte[] inBuf = new byte[UDPNetwork.MAX_UDP_PKT_SIZE];
				DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
				rpcSocket.receive(recvPkt);
				InetAddress returnAddr = recvPkt.getAddress();
				int returnPort = recvPkt.getPort();
				Object[] elements = Marshalling.unmarshall(inBuf);
				int returnServerPort = (Integer) elements[elements.length - 1];
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
						outBufStr = getMembers((Integer)elements[2]);
						break;
					default:
						break;						
				}
				String[] tempList = new String[outBufStr.length + 2];
				tempList[0] = (String) elements[0];
				tempList[1] = InetAddress.getLocalHost().getHostName()+":"+rpcSocket.getLocalPort();
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
			for (sessionKey o : myData.sessionState.keySet()) {
			    // ...
				if(o.getSessionID().equals(SID)){
					attemptAgain = o;
					break;
				}
			}
			if(attemptAgain != null) session_info = myData.sessionState.get(attemptAgain);
			
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
		// TODO BONUS; get this done then
		return null;
	}

}
