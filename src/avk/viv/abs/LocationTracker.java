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
import android.os.Message;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

public class LocationTracker   {

	public LocationManager gpsManager;
	LocationListener gpsListener;

	public LocationManager wifiManager;
	LocationListener wifiListener;
	Timer gsmTimer;
	
	public Context			context;
	public SharedPreferences prefs;
	public GatewayUtil gatewayUtil;
	
	class CommonListener implements LocationListener {

		public int updateCounter;
		
		public CommonListener() {
			this.updateCounter = 0;
		}
		
		public void onLocationChanged(Location location) {

			this.updateCounter++;

			NetLog.v("%s: Changed(%d): %s\n", location.getProvider(),this.updateCounter,location.toString());

			String beaconID = prefs.getString("beaconID", "0");
			
			GatewayUtil gw = new GatewayUtil(context);
			LocationObj locObj = new GPSOrNetworkLocationObj(beaconID,location,prefs.getString("statusText", "Нет Статуса..."));
			locObj.updateCount = updateCounter;
			
			if ( StatusActivity.updateHandler != null ) {
				Message msg = StatusActivity.updateHandler.obtainMessage(locObj.providerType,locObj);
				StatusActivity.updateHandler.sendMessage(msg);
			}
			
			if ( gw.saveLocation(locObj) )
				NetLog.v("%s: Failed to save location: %s",location.getProvider(),gw.responseMSG);
		
			gpsManager.removeUpdates(gpsListener);
			wifiManager.removeUpdates(wifiListener);
		}

		public void onProviderDisabled(String provider) {
			NetLog.v("%s: Disabled\n",provider);
		}

		public void onProviderEnabled(String provider) {
			NetLog.v("%s: Enabled\n",provider);
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			NetLog.v("%s: Status Changed to %d",provider,status);
			
		}
	}; // Common Listener
	
	
	public LocationTracker(Context context) {
		this.context = context;
		this.prefs   = context.getSharedPreferences("prefs", 1);
	}

	public void Init() {

		NetLog.v("LocationTracker initialized");
		
		gatewayUtil = new GatewayUtil(context);
		
		gpsManager = (LocationManager)this.context.getSystemService(Context.LOCATION_SERVICE);
		wifiManager = (LocationManager)this.context.getSystemService(Context.LOCATION_SERVICE);
		
		gpsListener = new CommonListener();
		wifiListener = new CommonListener();
		// Request out updates...
	}

	public void Done() {
		NetLog.v("LocationTracker released");
		gpsManager = null;
		wifiManager = null;
	}

	public void requestUpdate() {
		NetLog.v("LocationTacker: Requesting updates...\n");
		// запрашиваем апдейты по сети и спутнику
		gpsManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsListener);
		wifiManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, wifiListener);
		String beaconID = prefs.getString("beaconID", "0");
		
		// Сразу хватаем данные с вышек
		NetLog.v("Want's to update GSM location for %s",beaconID);
	    GSMLocationObj locObj = new GSMLocationObj(beaconID,context,prefs.getString("statusText","Нет статуса..."));
	    if ( !gatewayUtil.saveLocation(locObj) )
	    	NetLog.v("Failed to save GSM location: %s",gatewayUtil.responseMSG);
	    else  { NetLog.v("GSM Location: %s", gatewayUtil.responseMSG);
	    	if ( GsmStatusActivity.updateHandler != null ) {
	    		Message msg = GsmStatusActivity.updateHandler.obtainMessage(GatewayUtil.kGSM, locObj);
	    		GsmStatusActivity.updateHandler.sendMessage(msg);
	    	} // updateHandler != null
	    }
	}
}
