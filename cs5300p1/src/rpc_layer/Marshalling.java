package rpc_layer;

import java.io.UnsupportedEncodingException;

public class Marshalling {

	/**
	 * RPC marshalling function.
	 * @param objs Objects which properly implement toString. (ex: Integer, String, Double, custom class, etc)
	 * @return Byte array containing objects encoded with UTF-8 and Base64.
	 * @throws UnsupportedEncodingException 
	 */
	static public byte[] marshall(Object[] objs){
		String elt = "";
		for(Object o:objs){
			elt += o.toString() + ":";
		}
		try {
			return elt.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
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
	static public Object[] unmarshall(byte[] bytes){
		//This may return String[] instead.
		String s;
		try {
			s = new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
		
		s = s.replace("\0", "");
		String[] retArray = s.split(":");
		return retArray;
	}
}
