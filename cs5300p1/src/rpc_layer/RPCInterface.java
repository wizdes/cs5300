package rpc_layer;

import java.net.DatagramPacket;
import java.util.Set;

public interface RPCInterface {	
	/**
	 * 
	 * @param SID A session ID.
	 * @param version Version number of the requested session.
	 * @return Pair <found_version, data> or "not found"
	 */
	public byte[] sessionRead(String SID, String version);
	
	/**
	 * 
	 * @param SID A session ID
	 * @param version Version number of session to be stored
	 * @param data Session data to be stored 
	 * @param discardTime Time after which the stored session may be garbage collected.
	 * @return An acknowledgement.
	 */
	public byte[] sessionWrite(String SID, String version, String data, String discardTime);
	
	/**
	 * 
	 * @param SID A session ID
	 * @param version Version number of the requested session.
	 * @return An acknowledgement.
	 */
	public byte[] sessionDelete(String SID, String version);
	
	
	/**
	 * 
	 * @param sz Max number of members to be returned.
	 * @return subset of the mbrSet of the called server, chosen uniformly at random without replacement.
	 */
	public byte[] getMembers(int sz);
}
