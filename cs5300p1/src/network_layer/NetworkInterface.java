package network_layer;

import java.net.DatagramPacket;
import java.util.ArrayList;

public interface NetworkInterface {
	//Not sure what type destAddr will be...
	public void send(byte[] buffer, String destAddr, int destPort);
	
	//Wait to receive packet with specific call id
	public DatagramPacket receive(int callID);
	
	//Combination of send and receive
	public DatagramPacket sendAndWait(byte[] buffer, String destAddr, int destPort);
	
	//If broadcasting to entire topology
	public void broadcast(byte[] buffer);
	
	//If broadcasting to subset of topology
	public void broadcast(byte[] buffer, ArrayList<String> destAddresses, ArrayList<Integer> destPorts);
}
