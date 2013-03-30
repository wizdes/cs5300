package testCases;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;

import rpc_layer.*;
import data_layer.SessionData;

public class RPCJUnitTest {

	@Test
	public void test() throws UnknownHostException {
		//fail("Not yet implemented");
		SessionData myData = new SessionData();
		ClientStubs client = new ClientStubs();
		ServerStubs serverStub = new ServerStubs(myData); 
		Thread server = new Thread(serverStub);
		client.initClient(5000);
		server.start();
		
		DestinationAddressList dest = new DestinationAddressList();
		dest.addDestAddress(InetAddress.getLocalHost(), ServerStubs.getServerPort());
	
		System.out.println("READING.");					
		String[] resp = (String[]) Marshalling.unmarshall(client.sessionRead("2_192.168.1.2", "8", dest));
		for(String s:resp){
			System.out.println(s);
		}

		System.out.println("WRITING.");			
		System.out.println(client.sessionWrite("2_192.168.1.2", "8", "Hello Max and Patrick!", "dateVal",0));
		
	}
}
