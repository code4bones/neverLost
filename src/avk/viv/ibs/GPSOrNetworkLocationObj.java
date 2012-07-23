package avk.viv.ibs;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.location.Location;
import android.location.LocationManager;


public class GPSOrNetworkLocationObj extends LocationObj implements ILocationObj {
	
	public Location location = null;
	public boolean isGPS;
	
	public GPSOrNetworkLocationObj(String beaconID,Location location,String sStatus) {
		
		this.beaconID = beaconID;
		this.location = location;
		this.statusText  = sStatus;
		if ( this.location !=  null )
			this.isGPS  = this.location.getProvider().equals(LocationManager.GPS_PROVIDER);
		this.providerType = isGPS?GatewayUtil.kGPS:GatewayUtil.kWiFi;
	}
	
	public String getURL() {
		String sRequest = null;
		if ( isGPS )
	    	sRequest = "//shluz.tygdenakarte.ru:60080/cgi-bin/Location_02?document=<request><function><name>PHONEFUNC_PKG.saveLocation_Phone_GPS</name><index>1</index><param>%s^%f^%f^%f^%s^%s</param></function></request>";
	    else 
	    	sRequest = "//shluz.tygdenakarte.ru:60080/cgi-bin/Location_02?document=<request><function><name>PHONEFUNC_PKG.saveLocation_Phone_Wifi</name><index>1</index><param>%s^%f^%f^%f^%s^%s</param></function></request>";
	    return String.format(sRequest, beaconID,location.getLongitude(),location.getLatitude(),location.getAccuracy(),GatewayUtil.md5(GatewayUtil.deviceID),statusText);
	}
	
	public String getFile() {
		String locationWay;
		if( isGPS )
			locationWay = "1";
		else
			locationWay = "3";
		
		return String.format("%s^%s^%f^%f^%f^%s^%s^%s\n",locationWay,beaconID,location.getLongitude(),location.getLatitude(),location.getAccuracy(),GatewayUtil.md5(GatewayUtil.deviceID),statusText,new SimpleDateFormat("dd-MM-yyyy'_'HH:mm:ss").format(new Date()));
	}
}; // GPSOrNetworkLocationObj
