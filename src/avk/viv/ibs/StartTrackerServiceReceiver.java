package avk.viv.ibs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;



public class StartTrackerServiceReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		
	     SharedPreferences prefs = context.getSharedPreferences("prefs", 1);
	     
	     if ( prefs.getBoolean("active", false) == false ) {
	    	 NetLog.v("%s: Service is in inactive state....",this.getClass().getName());
	 		Intent service = new Intent(context, TrackerService.class);
	 		context.stopService(service);
	     } else { 
	    	 Intent service = new Intent(context, TrackerService.class);
	    	 context.startService(service);
	 	    NetLog.v("%s : Service restarted...",this.getClass().getName());
	     }
	}
}
