package rpc_layer;

public class Marshalling {

	/**
	 * RPC marshalling function.
	 * @param objs Objects which properlly implement toString. (ex: Integer, String, Double, custom class, etc)
	 * @return Byte array containing objects encoded with UTF-8 and Base64.
	 */
	static public byte[] marshall(Object[] objs){
		return null;
	}
	/**
	 * RPC demarshalling function.
	 * @param bytes Byte array containing objects encoded with UTF-8 and Base64.
	 * @return Objects which properlly implement toString. (ex: Integer, String, Double, custom class, etc)
	 */
	static public Object[] unmarshall(byte[] bytes){
		//This may return String[] instead.
		return null;
	}
}
