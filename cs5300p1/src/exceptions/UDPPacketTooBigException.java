package exceptions;

public class UDPPacketTooBigException extends Exception{
	private static final long serialVersionUID = 1L;

	public UDPPacketTooBigException(){
		super("The data you are trying to send does not fit" +
				"in a UDP packet.");
	}
}
