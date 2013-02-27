package coreservlets;

public class CookieContents extends DataContents {
	private String lm;

	public CookieContents(int sessionID, int version_number, String lm){
		super(sessionID, version_number);
		this.lm =  lm;
	}

	public String getLm() {
		return lm;
	}

	public void setLm(String lm) {
		this.lm = lm;
	}
	
}
