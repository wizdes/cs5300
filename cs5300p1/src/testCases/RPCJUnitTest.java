package testCases;

import static org.junit.Assert.*;

import org.junit.Test;

import rpc_layer.*;

public class RPCJUnitTest {

	@Test
	public void test() {
		//fail("Not yet implemented");
		ClientStubs client = new ClientStubs();
		ServerStubs server = new ServerStubs();
		client.initClient(5000);
	}

}
