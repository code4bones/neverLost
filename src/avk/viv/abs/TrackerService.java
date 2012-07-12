package avk.viv.abs;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


public class TrackerService extends Service {

	public class TrackerBinder extends Binder {
		TrackerService getService() {
			return TrackerService.this;
		}
	}
	
	private final IBinder trackerBinder = new TrackerBinder();
	
	@Override
	public void onCreate() 
	{
	  super.onCreate();
	  
	  
      Log.v("clinch","Service Started");
	}	

	@Override
	public int onStartCommand(Intent intent,int flags, int startId)
	{
		super.onStart(intent, startId);
		
		return START_STICKY;
	}
	
	//@Override
	public void run() 
	{
		// TODO Auto-generated method stub
	      //Toast.makeText(this, "Service run.", Toast.LENGTH_LONG).show();		
	}
	
	
	@Override
	public void onDestroy() 
	{
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return trackerBinder;
	}

	
}
