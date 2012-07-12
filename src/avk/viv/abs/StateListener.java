package avk.viv.abs;

import android.content.*;
import android.location.GpsStatus.NmeaListener;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;

public class StateListener  extends Handler {

	//final public PhoneStateListener phoneListener;
	//final public GpsStatus.Listener gpsStatusListener;
	public LocationManager locManager;
	public TelephonyManager phManager; 
	
	//public ConnectListener conListener;
	public PhoneListener   phoneListener;
	public GPSListener      gpsListener;
	public static Handler		   stateHandler;
	/*
	public static class ConnectListener extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			Log.v("clinch","Connectivity");
			Message msg = stateHandler.obtainMessage(); 
			msg.obj = new String("Senging Connectivity state");
			stateHandler.sendMessage(msg);
		}
	
	}
	*/
	private class PhoneListener extends PhoneStateListener {
		
		@Override
		public void onDataConnectionStateChanged(int state,int networkType) {
			Log.v("clinch","Phone");
			Message msg = stateHandler.obtainMessage(); 
			msg.obj = new String("Sending Phone State");
			stateHandler.sendMessage(msg);
		}
	}
	
	public static class GPSListener implements GpsStatus.Listener {

		public void onGpsStatusChanged(int event) {
			Log.v("clinch","GPS");
			Message msg = stateHandler.obtainMessage();
			msg.obj = new String("Senging GPS State");
			stateHandler.sendMessage(msg);
		}
	}
	
	public StateListener(Context context) {

			stateHandler = (Handler)this;
			
			//conListener   = new ConnectListener();
			phoneListener = new PhoneListener();
			gpsListener   = new GPSListener();
			
			
			phManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
			phManager.listen(phoneListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE | PhoneStateListener.LISTEN_SERVICE_STATE);
			
			
			locManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
			locManager.addGpsStatusListener(gpsListener);
			
			Log.v("clinch","Listeners created");
	}
	
}


