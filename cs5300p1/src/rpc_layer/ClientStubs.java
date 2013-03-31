package rpc_layer;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;


public class ClientStubs{
	
	private int callIDCounter;
	private int rpc_server_port;
	private DestinationAddressList clientAddresses;
	private Random random = new Random();
	public final static int UDPTimeOutms = 2000;
	public static final int MAX_UDP_PKT_SIZE = 512;	//Bytes
	
	public void initClient(int rpc_server_port){
		this.rpc_server_port=rpc_server_port;
		callIDCounter = 10000 * rpc_server_port;
		clientAddresses = new DestinationAddressList();
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
			retArray = new Object[3];
			retArray[0] = callID;
			retArray[1] = op;
			retArray[2] = new Integer(sz);
		}
		else if(op == OperationEnums.operationSESSIONWRITE){
			retArray = new Object[7];
			retArray[0] = callID;
			retArray[1] = op;
			retArray[2] = SID;
			retArray[3] = version;
			retArray[4] = data;
			retArray[5] = discardTime;
		}
		else{
			retArray = new Object[5];
			retArray[0] = callID;
			retArray[1] = op;
			retArray[2] = SID;
			retArray[3] = version;
		}
		retArray[retArray.length - 1] = new Integer(rpc_server_port);
		return retArray;		
	}
	
	public byte[] sessionAction(String SID, String version, String data,
			String discardTime, int sz, OperationEnums op, DestinationAddressList dest) { 
		DatagramPacket recvPkt = null;
		DatagramSocket rpcSocket = null;

		int callID = getUniqueCallID();
		byte[] outBuf = Marshalling.marshall(createArrayObjects(callID, op, SID, version, data, discardTime, sz));
		InetAddress addr=null;
		int portNum=-1;
		System.out.println("entering for");
		for(int i = 0; i < dest.size(); i++){
			try {
				rpcSocket = new DatagramSocket();
			} catch (SocketException e) {
				return null;
			}
			try {
				//I PICKED SENDING TWO SESSIONREADS by doing this
				//(so I don't have to put the timeout and do additional logic later)
				addr = dest.getDestAddr(i);
				portNum = dest.getDestPort(i);
				System.out.println(	addr.getHostAddress()+"=="+InetAddress.getLocalHost().getHostAddress()+" "+portNum+"=="+rpc_server_port);
				if (addr.getHostAddress().equals(InetAddress.getLocalHost().getHostAddress()) && portNum==rpc_server_port){
					continue;
					
				}
				System.out.println("Sending to "+addr+":"+portNum);
				DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, addr, portNum);	
				rpcSocket.send(sendPkt);
				byte[] inBuf = new byte[MAX_UDP_PKT_SIZE];
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
				System.out.println("timeOut");
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
					removeAddr(addr,portNum);
			}
			else {
				return recvPkt.getData();
			}
		}
		return null;
	}

	public byte[] sessionRead(String SID, String version, DestinationAddressList dest) { 
		System.out.println("Doing read");
		return sessionAction(SID, version, null, null, -1, OperationEnums.operationSESSIONREAD, dest);
	}

	public byte[] sessionWrite(String SID, String version, String data, String discardTime, int serverIndex) {
		System.out.println("Doing write");
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
	
	public void removeAddr(InetAddress addr, int port){
		clientAddresses.removeAddr(addr,port);
	}
	public byte[] sessionDelete(String SID, String version, DestinationAddressList dest) {
		System.out.println("Doing delete");
		return sessionAction(SID, version, null, null, -1, OperationEnums.operationDELETE, dest);
	}

	public byte[] getMembers(int sz, DestinationAddressList dest) {
		System.out.println("Doing getMembers");
		return sessionAction(null, null, null, null, sz, OperationEnums.operationGETMEMBERS, dest);
	}

	public void mergeList(DestinationAddressList dest){
		clientAddresses.mergeList(dest);
	}

	public DestinationAddressList getClientAddresses(){
		return clientAddresses;
	}
}
