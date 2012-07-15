package avk.viv.abs;

import android.content.Context;
import android.location.Criteria;
import android.location.LocationManager;

public class LocationObj extends Object implements ILocationObj {

	public String beaconID;
	public String statusText;
	public int    providerType;
	public int	  updateCount;
	
	public LocationObj() {
	}
	
	public String getURL() {
		return null;
	}
	public String getFile() {
		return null;
	}
	
	static String getBestProvider(LocationManager lm) {
		Criteria criteria = new Criteria();
		criteria.setAccuracy( Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired( false );
		criteria.setBearingRequired( false );
		criteria.setCostAllowed( true );
		criteria.setPowerRequirement( Criteria.NO_REQUIREMENT );
		return lm.getBestProvider( criteria, true);	        	
	}	
}; // Location Obj
