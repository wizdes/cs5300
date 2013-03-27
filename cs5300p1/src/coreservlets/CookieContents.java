package coreservlets;

import rpc_layer.DestinationAddressList;

/**
 * This object extends DataContents to contain cookie information; it adds
 * locationMetadata.
 */
public class CookieContents extends DataContents {
	private DestinationAddressList destinationAddressList;

	/**
	 * Constructs a CookieContents object
	 * 
	 * @param sessionID
	 *            The sessionID of the cookie
	 * @param versionNumber
	 *            The versionNumber of the cookie
	 * @param locationMetadata
	 *            The locationMetadata of the cookie
	 */
	public CookieContents(String sessionID, int versionNumber) {
		super(sessionID, versionNumber);
		destinationAddressList=null;
	}

	/**
	 * @return locationMetadata
	 */
	public String getLocationMetadata() {
		String output="";
		for (int i=0; i<destinationAddressList.size(); i++){
			output+=destinationAddressList.getDestAddr(i).getHostAddress()+":"+destinationAddressList.getDestPort(i);
			if(i<destinationAddressList.size()-1){
				output+=",";
			}
		}
		return output;
	}

	/**
	 * @param destinationAddressList
	 *            Sets destinationAddressList to destinationAddressList
	 */
	public void setDestinationAddressList(DestinationAddressList destinationAddressList) {
		this.destinationAddressList = destinationAddressList;
	}
	

	/**
	 * @return destinationAddressList
	 */
	public DestinationAddressList getDestinationAddressList() {
		return destinationAddressList;
	}
}
