package network_layer;

import java.net.DatagramPacket;
import java.util.ArrayList;

public class UDPNetwork implements NetworkInterface{

	@Override
	public void send(byte[] buffer, String destAddr, int destPort) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DatagramPacket receive(int callID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DatagramPacket sendAndWait(byte[] buffer, String destAddr,
			int destPort) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void broadcast(byte[] buffer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void broadcast(byte[] buffer, ArrayList<String> destAddresses,
			ArrayList<Integer> destPorts) {
		// TODO Auto-generated method stub
		
	}

}
