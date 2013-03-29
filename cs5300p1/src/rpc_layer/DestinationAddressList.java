package rpc_layer;

import java.net.InetAddress;
import java.net.UnknownHostException;
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
	
	public void mergeList(DestinationAddressList newList, int rpc_server_port){
		

		for(int i = 0; i < newList.destAddr.size();i++){
			try {
				if(!contains(newList.destAddr.get(i)) && 
					!newList.destAddr.get(i).equals(InetAddress.getLocalHost().getHostAddress()) 
					&& newList.destPort.get(i)!=rpc_server_port){
					destAddr.add(newList.destAddr.get(i));
					destPort.add(newList.destPort.get(i));
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
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
	
	public void removeAddr(InetAddress addr, int port){
		for (int i=0; i<destAddr.size(); i++){
			if(destAddr.get(i).equals(addr) && destPort.get(i)==port){
				destAddr.remove(i);
				destPort.remove(i);
			}
		}
	}
	
	public String toString(){
		String output="";
		for (int i=0; i<destAddr.size(); i++){
			output+=destAddr.get(i).getHostAddress()+":"+destPort.get(i);
		}
		return output;
	}
}
