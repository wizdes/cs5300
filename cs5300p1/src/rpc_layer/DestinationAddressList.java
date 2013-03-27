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
	
	public boolean contains(InetAddress da){
		if(destAddr.contains(da)) return true;
		return false;
		
	}
	
	public void mergeList(DestinationAddressList newList){
		for(int i = 0; i < newList.destAddr.size();i++){
			if(!contains(newList.destAddr.get(i))){
				destAddr.add(newList.destAddr.get(i));
				destPort.add(newList.destPort.get(i));
			}
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
