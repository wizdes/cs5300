package rpc_layer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

import network_layer.UDPNetwork;

import coreservlets.UserContents;

import data_layer.SessionData;
import rpc_layer.Marshalling;

public class ServerStubs extends Thread{
	DatagramSocket rpcSocket = null;
	SessionData myData = null;
	
	//returns the port number to be used on client stubs
	public ServerStubs(SessionData data){
		myData = data;
		try {
			rpcSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public int getServerPort(){
		return rpcSocket.getLocalPort();
	}
	public String getLocationMetaData(){
		return rpcSocket.getLocalAddress()+":"+rpcSocket.getLocalPort();
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
				String[] tempList = new String[outBufStr.length + 1];
				tempList[0] = (String) elements[0];
				System.arraycopy(outBufStr, 0, tempList, 1, outBufStr.length);
				
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
		ReentrantLock SessionLock = myData.sessionLocks.get(SID);
		if(SessionLock == null) return constructNotFoundResponse();

		try {
			SessionLock.lock();
			UserContents session_info = myData.sessionState.get(SID);
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
		finally{
			SessionLock.unlock();
		}
	}

	public String[] sessionWrite(String SID, String version, String data, String discardTime) {
		ReentrantLock SessionLock = myData.sessionLocks.get(SID);
		if(SessionLock == null){
			//create a new one
			myData.createNewSession(SID, Integer.parseInt(version), data, Long.parseLong(discardTime));
		}
		try {
			SessionLock.lock();
			UserContents session_info = myData.sessionState.get(SID);
			
			if(session_info == null){
				//create a new one				
				myData.createNewSession(SID, Integer.parseInt(version), data, Long.parseLong(discardTime));
			}
			
			session_info.setMessage(data);
			session_info.setExpirationTime(Long.parseLong(discardTime));
			String[] writtenResp = new String[1];
			writtenResp[0] = "Written";
			return writtenResp;		
		}
		finally{
			SessionLock.unlock();
		}
	}

	public String[] sessionDelete(String SID, String version) {
		ReentrantLock SessionLock = myData.sessionLocks.get(SID);
		if(SessionLock == null) return constructNotFoundResponse();
		try {
			SessionLock.lock();
			UserContents session_info = myData.sessionState.get(SID);
			if(session_info == null){
				return constructNotFoundResponse();
			}
			else{
				session_info.setExpirationTime(System.currentTimeMillis() / 1000);
				String[] writtenResp = new String[1];
				writtenResp[0] = "Deleted";
				return writtenResp;
			}
			
		}
		finally{
			SessionLock.unlock();
		}
	}

	public String[] getMembers(int sz) {
		// TODO Auto-generated method stub
		return null;
	}

}
