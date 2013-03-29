package rpc_layer;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;

import network_layer.UDPNetwork;

public class ClientStubs implements RPCInterface{
	
	private int callIDCounter;
	private int rpc_server_port;
	private DestinationAddressList clientAddresses;
	private Random random = new Random();
	public final static int UDPTimeOutms = 2000;
	
	public void initClient(int rpc_server_port){
		this.rpc_server_port=rpc_server_port;
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
	
	public Object[] createArrayObjects(int callID, OperationEnums op, String SID, String version,
			String data, String discardTime, int sz){
		Object[] retArray = null;
		if(op == OperationEnums.operationGETMEMBERS){
			retArray = new Object[1];
			retArray[0] = new Integer(sz);
		}
		else if(op == OperationEnums.operationSESSIONWRITE){
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
			String discardTime, int sz, OperationEnums op, DestinationAddressList dest) { 
		DatagramPacket recvPkt = null;
		DatagramSocket rpcSocket = null;

		try {
			rpcSocket = new DatagramSocket();
		} catch (SocketException e) {
			return null;
		}
		int callID = getUniqueCallID();
		byte[] outBuf = Marshalling.marshall(createArrayObjects(callID, op, SID, version, data, discardTime, sz));
		
		for(int i = 0; i < dest.size(); i++){
			try {
				//I PICKED SENDING TWO SESSIONREADS by doing this
				//(so I don't have to put the timeout and do additional logic later)
				InetAddress addr = dest.getDestAddr(i);
				int portNum = dest.getDestPort(i);
				System.out.println(	addr.getHostAddress()+"=="+InetAddress.getLocalHost().getHostAddress()+" "+portNum+"=="+rpc_server_port);

				if (addr.getHostAddress().equals(InetAddress.getLocalHost().getHostAddress()) && portNum==rpc_server_port){
					continue;
					
				}
				System.out.println("Sending to "+addr+":"+portNum);
				DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, addr, portNum);	
				rpcSocket.send(sendPkt);
				byte[] inBuf = new byte[UDPNetwork.MAX_UDP_PKT_SIZE];
				recvPkt = new DatagramPacket(inBuf, inBuf.length);
				Integer checkCallID = 0;
				do{
					recvPkt.setLength(inBuf.length);
					rpcSocket.setSoTimeout(UDPTimeOutms);
					rpcSocket.receive(recvPkt);
					checkCallID = Integer.parseInt((String) (Marshalling.unmarshall(inBuf)[0]));
				}
				while(checkCallID != callID);
			}
			catch(InterruptedIOException iioe){
				//timeout
				recvPkt = null;
			}
			catch(IOException ioe){
				//other error
				System.out.println("io exception");
				recvPkt = null;
			}
			finally{
				rpcSocket.close();
			}
			if (recvPkt==null){
					removeAddr(i);
			}
			else {
				return recvPkt.getData();
			}
		}
		return null;
	}

	@Override
	public byte[] sessionRead(String SID, String version, DestinationAddressList dest) { 
		return sessionAction(SID, version, null, null, -1, OperationEnums.operationSESSIONREAD, dest);
	}

	@Override
	public byte[] sessionWrite(String SID, String version, String data, String discardTime, int serverIndex) {
		if(clientAddresses.size() == 0) return null;
		DestinationAddressList dest = new DestinationAddressList();
		dest.addDestAddress(clientAddresses.getDestAddr(serverIndex), clientAddresses.getDestPort(serverIndex));
		
		return sessionAction(SID, version, data, discardTime, -1, OperationEnums.operationSESSIONWRITE, dest);
	}

	public int getRandomServerIndex(){
		return random.nextInt(clientAddresses.size());
	}
	public int getNumServers(){
		return clientAddresses.size();
	}
	
	public InetAddress getDestAddr(int index){
		return clientAddresses.getDestAddr(index);
	}
	public int getDestPort(int index){
		return clientAddresses.getDestPort(index);
	}
	
	public void removeAddr(int index){
		clientAddresses.removeAddr(index);
	}
	@Override
	public byte[] sessionDelete(String SID, String version, DestinationAddressList dest) {
		return sessionAction(SID, version, null, null, -1, OperationEnums.operationDELETE, dest);
	}

	@Override
	public byte[] getMembers(int sz, DestinationAddressList dest) {
		return sessionAction(null, null, null, null, sz, OperationEnums.operationGETMEMBERS, dest);
	}

	public void mergeList(DestinationAddressList dest){
		clientAddresses.mergeList(dest);
	}

	public DestinationAddressList getClientAddresses(){
		return clientAddresses;
	}
}
