package avk.viv.abs;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

public class LocationTracker /*extends TimerTask*/ /*implements Runnable*/  {

	public LocationManager gpsManager;
	LocationListener gpsListener;

	public LocationManager wifiManager;
	LocationListener wifiListener;
	Timer gsmTimer;
	
	public Context			context;
	public BeaconObj		beaconObj;
	public SharedPreferences prefs;
	public GatewayUtil gatewayUtil;
	
	class CommonListener implements LocationListener {
		
		public String sProvider;
		
		public CommonListener(String sProvider) {
			this.sProvider = sProvider;
		}
		
		public void onLocationChanged(Location location) {
			NetLog.v("%s: Changed: %s\n", sProvider,location.toString());

			String beaconID = prefs.getString("beaconID", "0");
			
			GatewayUtil gw = new GatewayUtil(context);
			LocationObj locObj = new GPSOrNetworkLocationObj(beaconID,location,"no status");
			
			if ( gw.saveLocation(locObj) )
				NetLog.v("%s: Failed to save location: %s",sProvider,gw.responseMSG);
		}

		public void onProviderDisabled(String provider) {
			NetLog.v("%s: Disabled\n",provider);
		}

		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			NetLog.v("%s: Enabled\n",provider);
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			NetLog.v("%s: Status Changed to %d",provider,status);
			
		}
	}; // Common Listener
	
	class UpdateGSM extends TimerTask {
		
		public void run() {
			String beaconID = prefs.getString("beaconID", "0");
			NetLog.v("Want's to update GSM location for %s",beaconID);
		    GSMLocationObj locObj = new GSMLocationObj(beaconID,context,"no gsm status");
		    if ( gatewayUtil.saveLocation(locObj) == false )
		    	NetLog.v("Failed to save GSM location: %s",gatewayUtil.responseMSG);
		}
	};
	
	public LocationTracker(Context context,SharedPreferences prefs) {
		this.context = context;
		this.prefs   = prefs;
	}

	public void Init() {

		NetLog.v("LocationTracker initialized");
		
		gatewayUtil = new GatewayUtil(context);
		
		gpsManager = (LocationManager)this.context.getSystemService(Context.LOCATION_SERVICE);
		wifiManager = (LocationManager)this.context.getSystemService(Context.LOCATION_SERVICE);
		
		gpsListener = new CommonListener(LocationManager.GPS_PROVIDER);
		wifiListener = new CommonListener(LocationManager.NETWORK_PROVIDER);
		// Request out updates...
	}

	public void Done() {
		NetLog.v("LocationTracker released");
		gpsManager = null;
		wifiManager = null;
		gsmTimer.cancel();
		gsmTimer = null;
	}

	public void run() {
		NetLog.v("LocationTacker Run\n");
		gpsManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsListener);
		wifiManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, wifiListener);
		gsmTimer = new Timer();
		UpdateGSM updateGSM = new UpdateGSM();
		gsmTimer.schedule(updateGSM, 0, 5000);
	}
}
