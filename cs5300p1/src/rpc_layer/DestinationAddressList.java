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
		if(!contains(newAddr,newPort)){
				destAddr.add(newAddr);
				destPort.add(newPort);
				System.out.println("adding "+newAddr+":"+newPort+" to mbr");
		}
		else {
			System.out.println("not adding "+newAddr+":"+ServerStubs.getServerPort()+" to mbr");
		}
	}
	
	public void addList(DestinationAddressList newList){
		for(InetAddress address:newList.destAddr){
			destAddr.add(address);
		}
		for(Integer port:newList.destPort){
			destPort.add(port);
		}
	}
	
	public boolean contains(InetAddress da, int port){
		if(destAddr.contains(da) && destPort.contains(port)) return true;
		return false;
		
	}
	
	public void mergeList(DestinationAddressList newList){
		

		for(int i = 0; i < newList.destAddr.size();i++){
			try {
				if(!contains(newList.destAddr.get(i),newList.destPort.get(i)) && 
					!newList.destAddr.get(i).equals(InetAddress.getLocalHost().getHostAddress()) 
					&& newList.destPort.get(i)!=ServerStubs.getServerPort()){
					destAddr.add(newList.destAddr.get(i));
					destPort.add(newList.destPort.get(i));
					System.out.println("adding "+newList.destAddr.get(i).getHostAddress()+":"+newList.destPort.get(i)+" to mbr");
				}
				else {
					System.out.println("not adding "+newList.destAddr.get(i).getHostAddress()+":"+newList.destPort.get(i)+" to mbr");
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
				System.out.println("removing "+addr.getHostAddress()+":"+port+" from mbr");
			}
		}
	}
	
	public String toString(){
		String output="";
		for (int i=0; i<destAddr.size(); i++){
			output+=destAddr.get(i).getHostAddress()+":"+destPort.get(i);
			if(i<(destAddr.size()-1)){
				output+=", ";
			}
		}
		return output;
	}
}
