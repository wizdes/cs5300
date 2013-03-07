package rpc_layer;

import java.util.Set;

public interface RPCInterface {	
	/**
	 * 
	 * @param SID A session ID.
	 * @param version Version number of the requested session.
	 * @return Pair <found_version, data> or "not found"
	 */
	public String sessionRead(String SID, String version);
	
	/**
	 * 
	 * @param SID A session ID
	 * @param version Version number of session to be stored
	 * @param data Session data to be stored 
	 * @param discardTime Time after which the stored session may be garbage collected.
	 * @return An acknowledgement.
	 */
	public String sessionWrite(String SID, String version, String data, String discardTime);
	
	/**
	 * 
	 * @param SID A session ID
	 * @param version Version number of the requested session.
	 * @return An acknowledgement.
	 */
	public String sessionDelete(String SID, String version);
	
	
	/**
	 * 
	 * @param sz Max number of members to be returned.
	 * @return subset of the mbrSet of the called server, chosen uniformly at random without replacement.
	 */
	public Set<String> getMembers(int sz);
}
