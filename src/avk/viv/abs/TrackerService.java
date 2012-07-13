package avk.viv.abs;
import java.util.Timer;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


public class TrackerService extends Service implements Runnable {

	public static BeaconObj beaconObj = null;
	public LocationManager locMgr = null;
	public LocationTracker tracker = null;
	UpdateLocation updateLocation = null;
	
	public class TrackerBinder extends Binder {
		TrackerService getService() {
			NetLog.v("Service: TrackerBinder.getService()");
			return TrackerService.this;
		}
	}
	
	private final IBinder trackerBinder = new TrackerBinder();
	
	@Override
	public void onCreate() 
	{
	  super.onCreate();
      NetLog.v("Service: I'm Created...");
	} // onCreate

	@Override
	public int onStartCommand(Intent intent,int flags, int startId)
	{
		super.onStartCommand(intent,flags, startId);
		NetLog.v("Service: onStartCommand");

		
		SharedPreferences prefs = this.getSharedPreferences("prefs", 1);
		NetLog.v("Service: login = %s,password = %s,beaconID = %s,beaconName = %s interval = %d",
				prefs.getString("login", ""),
				prefs.getString("password", ""),
				prefs.getString("beaconID","0"),
				prefs.getString("beaconName", ""),
				prefs.getInt("interval", 10));
		
	    Context context = this.getApplicationContext();
		
	    if ( updateLocation == null )
	    	updateLocation = new UpdateLocation();
	    
		updateLocation.Start(context);
		
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() 
	{
		updateLocation.Stop(this.getApplicationContext());
		updateLocation = null;
		NetLog.v("Service: OnDestroy");
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		NetLog.v("Service: onBind");
		return trackerBinder;
	}
	
	// @Override
	public void run() {
		
	}

	
}
