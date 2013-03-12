package exceptions;

public class UDPPacketTooBigException extends Exception{
	public UDPPacketTooBigException(){
		super("The data you are trying to send does not fit" +
				"in a UDP packet.");
	}
}
