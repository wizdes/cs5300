package network_layer;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import exceptions.UDPPacketTooBigException;

public class UDPNetwork implements NetworkInterface{
	public static final int MAX_UDP_PKT_SIZE = 512;	//Bytes
	private DatagramSocket socket = null;
	public UDPNetwork(int port){
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void send(byte[] buffer, InetAddress destAddr, int destPort) throws UDPPacketTooBigException, IOException{
		if( buffer.length > MAX_UDP_PKT_SIZE){
			throw new UDPPacketTooBigException();
		}
		
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, destAddr, destPort);
		socket.send(packet);
	}

	@Override
	public DatagramPacket receive(int callID) {
		//ASSUMPTION: CALL ID IS FIRST 4 BYTES 
		byte[] buffer = new byte[MAX_UDP_PKT_SIZE];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

		int recvCallID;
		try {
			do {
				packet.setLength(buffer.length);
				socket.receive(packet);
				recvCallID = ByteBuffer.wrap(buffer).getInt();
			} while (recvCallID != callID);
		} catch(InterruptedIOException iioe) {
		    // timeout 
		    packet = null;
		}catch (IOException e) { 
			e.printStackTrace();
			return null;
		}
		return packet;
	}

	@Override
	public DatagramPacket sendAndWait(byte[] buffer, InetAddress destAddr,
			int destPort, int callID)  throws UDPPacketTooBigException, IOException{
		send(buffer, destAddr, destPort);
		return receive(callID);
	}

	@Override
	public void broadcast(byte[] buffer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void broadcast(byte[] buffer, ArrayList<InetAddress> destAddresses,
			ArrayList<Integer> destPorts) throws UDPPacketTooBigException, IOException{
		assert destAddresses.size() == destPorts.size();
		
		for(int i = 0; i < destAddresses.size(); i++){
			send(buffer, destAddresses.get(i), destPorts.get(i));
		}
	}

}
