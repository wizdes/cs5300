  public class ValueElt{
  	public ValueElt(String string, String string2, String string3) {
		// TODO Auto-generated constructor stub
  		source = string;
  		PR = Double.parseDouble(string2);
  		deg = Integer.parseInt(string3);
	}
  	public ValueElt(String string, String string2, String string3, String string4) {
		// TODO Auto-generated constructor stub
  		source = string;
  		dest = string2;
  		PR = Double.parseDouble(string3);
  		deg = Integer.parseInt(string4);
	}
	public String source;
	public String dest;
  	public double PR;
  	public int deg;
  }