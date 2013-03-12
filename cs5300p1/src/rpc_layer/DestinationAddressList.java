package rpc_layer;

import java.net.InetAddress;
import java.util.ArrayList;

public class DestinationAddressList {
	private ArrayList<InetAddress> destAddr;
	private ArrayList<Integer> destPort;
	public DestinationAddressList(){
		destAddr = new ArrayList<InetAddress>();
		destPort = new ArrayList<Integer>();
	}
	
	public void addDestAddress(InetAddress newAddr, int newPort){
		destAddr.add(newAddr);
		destPort.add(newPort);
	}
	
	public void addList(DestinationAddressList newList){
		for(InetAddress address:newList.destAddr){
			destAddr.add(address);
		}
		for(Integer port:newList.destPort){
			destPort.add(port);
		}
	}
	
	public int size(){
		return destAddr.size();
	}
	
	public InetAddress getDestAddr(int index){
		return destAddr.get(index);
	}
	public Integer getDestPort(int index){
		return destPort.get(index);
	}
}
