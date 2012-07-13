package avk.viv.abs;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.location.Location;
import android.location.LocationManager;


public class GPSOrNetworkLocationObj extends LocationObj implements ILocationObj {
	
	public Location location;
	public boolean isGPS;
	
	public GPSOrNetworkLocationObj(String beaconID,Location location,String sStatus) {

		this.beaconID = beaconID;
		this.location = location;
		this.sStatus  = sStatus;
		this.isGPS    = location.getProvider().equals(LocationManager.GPS_PROVIDER);
	
	}
	
	public String getURL() {
		String sRequest = null;
		if ( isGPS )
	    	sRequest = "//shluz.tygdenakarte.ru:60080/cgi-bin/Location_02?document=<request><function><name>PHONEFUNC_PKG.saveLocation_Phone_GPS</name><index>1</index><param>%s^%f^%f^%f^-%s^%s</param></function></request>";
	    else 
	    	sRequest = "//shluz.tygdenakarte.ru:60080/cgi-bin/Location_02?document=<request><function><name>PHONEFUNC_PKG.saveLocation_Phone_Wifi</name><index>1</index><param>%s^%f^%f^%f^-%s^%s</param></function></request>";
	    return String.format(sRequest, beaconID,location.getLongitude(),location.getLatitude(),location.getAccuracy(),GatewayUtil.md5(GatewayUtil.deviceID),sStatus);
	}
	
	public String getFile() {
		return String.format("-1^%s^%f^%f^%f^-%s^%s^%s\n",beaconID,location.getLongitude(),location.getLatitude(),location.getAccuracy(),GatewayUtil.md5(GatewayUtil.deviceID),sStatus,new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()));
	}
}; // GPSOrNetworkLocationObj
