package network_layer;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class UDPNetwork implements NetworkInterface{
	private static final int MAX_UDP_PKT_SIZE = 512;	//Bytes
	DatagramSocket socket = null;
	public UDPNetwork(int port){
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	@Override
	public boolean send(byte[] buffer, InetAddress destAddr, int destPort) {
		assert buffer.length <= MAX_UDP_PKT_SIZE;
		
		DatagramPacket pkt = new DatagramPacket(buffer, buffer.length, destAddr, destPort);
		try {
			socket.send(pkt);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public DatagramPacket receive(int callID) {
		//ASSUMPTION: CALL ID IS FIRST 4 BYTES 
		byte[] buffer = new byte[MAX_UDP_PKT_SIZE];
		DatagramPacket pkt = new DatagramPacket(buffer, buffer.length);

		int recvCallID;
		try {
			do {
				pkt.setLength(buffer.length);
				socket.receive(pkt);
				recvCallID = ByteBuffer.wrap(buffer).getInt();
			} while (recvCallID != callID);
		} catch(InterruptedIOException iioe) {
		    // timeout 
		    pkt = null;
		}catch (IOException e) { 
			e.printStackTrace();
			return null;
		}
		return pkt;
	}

	@Override
	public DatagramPacket sendAndWait(byte[] buffer, InetAddress destAddr,
			int destPort, int callID) {
		send(buffer, destAddr, destPort);
		return receive(callID);
	}

	@Override
	public void broadcast(byte[] buffer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void broadcast(byte[] buffer, ArrayList<InetAddress> destAddresses,
			ArrayList<Integer> destPorts) {
		assert destAddresses.size() == destPorts.size();
		
		for(int i = 0; i < destAddresses.size(); i++){
			boolean successfullySend = send(buffer, destAddresses.get(i), destPorts.get(i));
		}
	}

}
