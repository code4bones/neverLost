package avk.viv.abs;

import java.lang.*;
import java.util.StringTokenizer;

import android.util.Log;

public class BeaconObj {
	
	public String name;
	public String uid;
	public String date;
	public Double latitude;
	public Double longitude;
	public String status;
	public Double accuracy;
	
	@Override
	public String toString() {
		String str = String.format("Name: %s,beaconID: %s,LAT:%f,LNG:%f,ACC:%f,DATE:%s,STATUS:%s", name,uid,latitude,longitude,accuracy,date,status);
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
		BeaconObj obj = new BeaconObj();
		
		StringTokenizer tokens = new StringTokenizer(src,"^");
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
