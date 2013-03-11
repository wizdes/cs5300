package rpc_layer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.locks.ReentrantLock;

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
	public void run(){
		while(true){
			try {
				byte[] inBuf = new byte[512];
				DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
				rpcSocket.receive(recvPkt);
				InetAddress returnAddr = recvPkt.getAddress();
				int returnPort = recvPkt.getPort();
				Object[] elements = Marshalling.unmarshall(inBuf);
				operationEnums operationCode = (operationEnums) elements[1];
				byte[] outBuf = null;
				switch(operationCode){
					case operationSESSIONREAD:
						outBuf = sessionRead((String)elements[2], (String)elements[3]);
						break;
					case operationSESSIONWRITE:
						outBuf = sessionWrite((String)elements[2], (String)elements[3], (String)elements[4], (String)elements[5]);
						break;
					case operationDELETE:
						outBuf = sessionDelete((String)elements[2], (String)elements[3]);
						break;
					case operationGETMEMBERS:
						outBuf = getMembers((Integer)elements[2]);
						break;
					default:
						break;						
				}
				DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, returnAddr, returnPort);
				rpcSocket.send(sendPkt);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}


	public byte[] sessionRead(String SID, String version) {
		ReentrantLock SessionLock = myData.sessionLocks.get(SID);
		SessionLock.lock();
		try {
			UserContents session_info = myData.sessionState.get(SID);
			if(session_info == null){
				SessionLock.unlock();
				String[] notFoundResp = new String[1];
				notFoundResp[0] = "Not found";
				return Marshalling.marshall(notFoundResp);		
			}
			else{
				String[] foundResp = new String[2];
				foundResp[0] = Integer.toString(session_info.getVersionNumber());
				foundResp[1] = session_info.getMessage();
				return Marshalling.marshall(foundResp);
			}
			
		}catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		finally{
			SessionLock.unlock();
		}
		
		return null;
	}

	public byte[] sessionWrite(String SID, String version, String data,
			String discardTime) {
		ReentrantLock SessionLock = myData.sessionLocks.get(SID);
		SessionLock.lock();
		try {
			UserContents session_info = myData.sessionState.get(SID);
			if(session_info == null){
				SessionLock.unlock();
				String[] notFoundResp = new String[1];
				notFoundResp[0] = "Not found";
				return Marshalling.marshall(notFoundResp);		
			}
			else{
				session_info.setMessage(data);
				session_info.setExpirationTime(Long.parseLong(discardTime));
				String[] writtenResp = new String[1];
				writtenResp[0] = "Written";
				return Marshalling.marshall(writtenResp);		
			}
			
		}catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		finally{
			SessionLock.unlock();
		}
		
		return null;
	}

	public byte[] sessionDelete(String SID, String version) {
		ReentrantLock SessionLock = myData.sessionLocks.get(SID);
		SessionLock.lock();
		try {
			UserContents session_info = myData.sessionState.get(SID);
			if(session_info == null){
				SessionLock.unlock();
				String[] notFoundResp = new String[1];
				notFoundResp[0] = "Not found";
	
					return Marshalling.marshall(notFoundResp);		
			}
			else{
				session_info.setExpirationTime(System.currentTimeMillis() / 1000);
				String[] writtenResp = new String[1];
				writtenResp[0] = "Deleted";
				return Marshalling.marshall(writtenResp);	
			}
			
		}catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		finally{
			SessionLock.unlock();
		}
		
		return null;
	}

	public byte[] getMembers(int sz) {
		// TODO Auto-generated method stub
		return null;
	}

}
