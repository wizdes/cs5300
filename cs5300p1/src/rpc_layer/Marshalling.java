package rpc_layer;

import java.io.UnsupportedEncodingException;

public class Marshalling {

	/**
	 * RPC marshalling function.
	 * @param objects Objects which properly implement toString. (ex: Integer, String, Double, custom class, etc)
	 * @return Byte array containing objects encoded with UTF-8 and Base64.
	 * @throws UnsupportedEncodingException 
	 */
	static public byte[] marshall(Object[] objects){
		String output = "";
		for(Object object:objects){
			output += object.toString() + ";";
		}
		try {
			return output.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			
		}
		return null;
	}
	/**
	 * RPC demarshalling function.
	 * @param bytes Byte array containing objects encoded with UTF-8 and Base64.
	 * @return Objects which properlly implement toString. (ex: Integer, String, Double, custom class, etc)
	 * @throws UnsupportedEncodingException 
	 */
	static public String[] unmarshall(byte[] bytes){
		//This may return String[] instead.
		String string;
		try {
			string = new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
		
		string = string.replace("\0", "");
		String[] retArray = string.split(";");
		return retArray;
	}
}
