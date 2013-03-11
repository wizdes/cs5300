package rpc_layer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Set;

import data_layer.SessionData;
import rpc_layer.Marshalling;

public class ServerStubs extends Thread implements RPCInterface {
	DatagramSocket rpcSocket = null;
	SessionData myData = null;
	
	//returns the port number to be used on client stubs
	public ServerStubs(SessionData data){
		myData = data;
		try {
			rpcSocket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}


	@Override
	public byte[] sessionRead(String SID, String version) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] sessionWrite(String SID, String version, String data,
			String discardTime) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] sessionDelete(String SID, String version) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getMembers(int sz) {
		// TODO Auto-generated method stub
		return null;
	}

}
