package network_layer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;

import exceptions.UDPPacketTooBigException;

public interface NetworkInterface {
	/**
	 * Sends buffer to destAddr:destPort
	 * @param buffer The buffer to send
	 * @param destAddr The IPv4 Address to send the buffer to
	 * @param destPort The port to send the buffer to
	 * @throws UDPPacketTooBigException if buffer.length > UDPNetwork.MAX_UDP_PKT_SIZE
	 * @throws IOException If there is an underlying error in sending the packet
	 */
	public void send(byte[] buffer, InetAddress destAddr, int destPort) throws UDPPacketTooBigException, IOException;
	
	//Wait to receive packet with specific call id
	public DatagramPacket receive(int callID);
	
	//Combination of send and receive
	public DatagramPacket sendAndWait(byte[] buffer, InetAddress destAddr, int destPort, int callID)  throws UDPPacketTooBigException, IOException;
	
	//If broadcasting to entire topology
	public void broadcast(byte[] buffer);
	
	//If broadcasting to subset of topology
	public void broadcast(byte[] buffer, ArrayList<InetAddress> destAddresses, ArrayList<Integer> destPorts) throws UDPPacketTooBigException, IOException;
}
