package avk.viv.abs;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


public class TrackerService extends Service implements Runnable {

	public static BeaconObj beaconObj = null;
	
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
	}	

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
		
		return START_STICKY;
	}
	
	//@Override
	public void run() 
	{
		// TODO Auto-generated method stub
	      //Toast.makeText(this, "Service run.", Toast.LENGTH_LONG).show();		
		Log.v("clinch","Service run()");
	}
	
	
	@Override
	public void onDestroy() 
	{
		Log.v("clinch","Service OnDestroy");
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		Log.v("clinch","onBind");
		return trackerBinder;
	}

	
}
