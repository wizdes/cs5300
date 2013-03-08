package rpc_layer;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import rpc_layer.Marshalling;
import rpc_layer.DestinationAddressList;
import rpc_layer.operationEnums;

public class ClientStubs implements RPCInterface{
	
	int callIDCounter = 0;
	DestinationAddressList clientAddresses;
	int maxPacketSize = 512;
	
	public void initClient(int rpc_server_port){
		callIDCounter = 10000 * rpc_server_port;
		clientAddresses = new DestinationAddressList();
	}
	
	public void addDestinationPort(DestinationAddressList newAddresses){
		clientAddresses.addList(newAddresses);
	}
	
	public int getUniqueCallID(){
		synchronized(this){
			return callIDCounter++;
		}
	}
	
	public Object[] createArrayObjects(Integer callID, operationEnums op, String SID, String version,
			String data, String discardTime, int sz){
		Object[] retArray = null;
		if(op == operationEnums.operationGETMEMBERS){
			retArray = new Object[1];
			retArray[0] = new Integer(sz);
		}
		else if(op == operationEnums.operationSESSIONWRITE){
			retArray = new Object[6];
			retArray[0] = callID;
			retArray[1] = op;
			retArray[2] = SID;
			retArray[3] = version;
			retArray[4] = data;
			retArray[5] = discardTime;
		}
		else{
			retArray = new Object[4];
			retArray[0] = callID;
			retArray[1] = op;
			retArray[2] = SID;
			retArray[3] = version;
		}
		return retArray;		
	}
	
	public byte[] sessionAction(String SID, String version, String data,
			String discardTime, int sz, operationEnums op) { 
		DatagramPacket recvPkt = null;
		DatagramSocket rpcSocket = null;
		try {
			rpcSocket = new DatagramSocket();

			int callID = getUniqueCallID();
			byte[] outBuf = Marshalling.marshall(createArrayObjects(callID, op, SID, version, data, discardTime, sz));
			for(int i = 0; i < clientAddresses.size(); i++){
				//THIS IS WRONG.
				//SEND IT ONLY TO THE RELEVANT PEOPLE
				//YOU CAN FIGURE THIS OUT FROM IPP_Primary, IPP_Backup (SID)
				//MAKE AN IF STATEMENT TO CHECK THIS THE CLIENT ADDRESSES HERE
				//I PICKED SENDING TWO SESSIONREADS (so I don't have to put the timeout and do additional logic later)
				InetAddress addr = clientAddresses.destAddr.get(i);
				int portNum = clientAddresses.destPort.get(i);
				DatagramPacket sendPkt = new DatagramPacket(outBuf, 512, addr, portNum);	
				rpcSocket.send(sendPkt);
			}
			byte[] inBuf = new byte[maxPacketSize];
			recvPkt = new DatagramPacket(inBuf, inBuf.length);
			try{
				Integer checkCallID = 0;
				do{
					recvPkt.setLength(inBuf.length);
					rpcSocket.receive(recvPkt);
					checkCallID = (Integer)(Marshalling.unmarshall(inBuf)[0]);
				}
				while(checkCallID != callID);
			}
			catch(InterruptedIOException iioe){
				//timeout
				recvPkt = null;
			}
			catch(IOException ioe){
				//other error
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			rpcSocket.close();
		}
		return recvPkt.getData();
	}

	@Override
	public byte[] sessionRead(String SID, String version) { 
		return sessionAction(SID, version, null, null, -1, operationEnums.operationSESSIONREAD);
	}

	@Override
	public byte[] sessionWrite(String SID, String version, String data,
			String discardTime) {
		return sessionAction(SID, version, data, discardTime, -1, operationEnums.operationSESSIONWRITE);
	}

	@Override
	public byte[] sessionDelete(String SID, String version) {
		return sessionAction(SID, version, null, null, -1, operationEnums.operationDELETE);
	}

	@Override
	public byte[] getMembers(int sz) {
		return sessionAction(null, null, null, null, sz, operationEnums.operationGETMEMBERS);
	}

}
