package avk.viv.abs;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;


public class OnBootReceiver extends BroadcastReceiver {

	//public static final long REPEAT_TIME = 1000 * 600;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
		     SharedPreferences prefs = context.getSharedPreferences("prefs", 1);
		     if ( prefs.getBoolean("active", false) == false ) {
		    	 NetLog.v("Service is in inactive state....");
		    	 return;
		     }
		     
			 AlarmManager service = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		     Intent i = new Intent(context,StartTrackerServiceReceiver.class);
		     PendingIntent pending = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		     Calendar cal = Calendar.getInstance();
		     cal.add(Calendar.SECOND, 1);
		     service.setInexactRepeating(AlarmManager.RTC_WAKEUP,
						cal.getTimeInMillis(), prefs.getInt("interval",10) * 60 * 1000, pending);
				
		     NetLog.v("%s : Service started while device boot completed.",this.getClass().getName());
		}		
	}
}
