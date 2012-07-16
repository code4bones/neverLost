package avk.viv.abs;

import java.util.StringTokenizer;

public class BeaconObj extends Object {
	
	public String login;
	public String password;
	public String name;
	public String uid;
	public String date;
	public Double latitude;
	public Double longitude;
	public String status;
	public Double accuracy;
	public int	  interval;
	public int	  selectedBeaconIndex;
	
	@Override
	public String toString() {
		String str = String.format("Login: %s, Password: %s, beaconName: %s, beaconID: %s,Interval: %d,LAT:%f,LNG:%f,ACC:%f,DATE:%s,STATUS:%s",
				login,password,name,uid,interval,latitude,longitude,accuracy,date,status);
		return str;
	}
	
	public static BeaconObj createWithString(String src) {
		BeaconObj obj = new BeaconObj();
		
		int start = src.indexOf("(");
		int end = src.lastIndexOf(")");
		
		obj.name = src.substring(0, start);
		obj.uid  = src.substring(start+1, end);
		
		return obj;
	}


	public static BeaconObj createWithLocationString(String src) {
		
		if ( src == null )
			return null;
		
		StringTokenizer tokens = new StringTokenizer(src,"^");
		if ( tokens.countTokens() < 4 )
			return null;
		
		BeaconObj obj = new BeaconObj();
		int nToken = 0;
		while ( tokens.hasMoreTokens() ) {
			String tok = tokens.nextToken();
			switch ( nToken ) {
			case 0: obj.latitude  = Double.parseDouble(tok); break;
			case 1: obj.longitude = Double.parseDouble(tok);break;
			case 2: obj.accuracy  = Double.parseDouble(tok);break;
			case 3: obj.date = tok;break;
			case 4: obj.status = tok;break;
				default:
					break;
			}
			nToken++;
		}
		return obj;
	}

}
