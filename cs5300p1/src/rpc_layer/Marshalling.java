package rpc_layer;

import java.io.UnsupportedEncodingException;

public class Marshalling {

	/**
	 * RPC marshalling function.
	 * @param objs Objects which properly implement toString. (ex: Integer, String, Double, custom class, etc)
	 * @return Byte array containing objects encoded with UTF-8 and Base64.
	 * @throws UnsupportedEncodingException 
	 */
	static public byte[] marshall(Object[] objs) throws UnsupportedEncodingException{
		String elt = "";
		for(Object o:objs){
			elt += o.toString() + "|";
		}
		return elt.getBytes("UTF-8");
	}
	/**
	 * RPC demarshalling function.
	 * @param bytes Byte array containing objects encoded with UTF-8 and Base64.
	 * @return Objects which properlly implement toString. (ex: Integer, String, Double, custom class, etc)
	 * @throws UnsupportedEncodingException 
	 */
	static public Object[] unmarshall(byte[] bytes) throws UnsupportedEncodingException{
		//This may return String[] instead.
		String s = new String(bytes, "US-ASCII");
		String[] retArray = s.split("|");
		return retArray;
	}
}
