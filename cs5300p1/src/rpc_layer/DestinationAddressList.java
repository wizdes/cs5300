package rpc_layer;

import java.net.InetAddress;
import java.util.ArrayList;

public class DestinationAddressList {
	ArrayList<InetAddress> destAddr;
	ArrayList<Integer> destPort;
	public void DestinationAddressList(){
		destAddr = new ArrayList<InetAddress>();
		destPort = new ArrayList<Integer>();
	}
	
	public void addDestAddress(InetAddress newAddr, int newPort){
		destAddr.add(newAddr);
		destPort.add(newPort);
	}
	
	public void addList(DestinationAddressList newList){
		for(InetAddress elt:newList.destAddr){
			destAddr.add(elt);
		}
		for(Integer elt:newList.destPort){
			destPort.add(elt);
		}
	}
	
	public int size(){
		return destAddr.size();
	}
}
