package network_layer;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;

public interface NetworkInterface {
	public boolean send(byte[] buffer, InetAddress destAddr, int destPort);
	
	//Wait to receive packet with specific call id
	public DatagramPacket receive(int callID);
	
	//Combination of send and receive
	public DatagramPacket sendAndWait(byte[] buffer, InetAddress destAddr, int destPort, int callID);
	
	//If broadcasting to entire topology
	public void broadcast(byte[] buffer);
	
	//If broadcasting to subset of topology
	public void broadcast(byte[] buffer, ArrayList<InetAddress> destAddresses, ArrayList<Integer> destPorts);
}
