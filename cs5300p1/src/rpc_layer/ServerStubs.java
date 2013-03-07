package rpc_layer;

import java.util.Set;

public class ServerStubs implements RPCInterface{

	@Override
	public String sessionRead(String SID, String version) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String sessionWrite(String SID, String version, String data,
			String discardTime) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String sessionDelete(String SID, String version) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getMembers(int sz) {
		// TODO Auto-generated method stub
		return null;
	}

}
