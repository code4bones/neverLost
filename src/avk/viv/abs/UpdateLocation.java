package avk.viv.abs;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;



public class UpdateLocation extends BroadcastReceiver {

	LocationTracker tracker = null;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if ( tracker == null ) {
			tracker = new LocationTracker(context);
			tracker.Init();
		}
		NetLog.v("UpdateLocation - Requesting positions\n");
		tracker.requestUpdate();
	}
	
	public void Start(Context context) {
		AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, UpdateLocation.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);		
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 20, pi); // Millisec * Second * Minute
		NetLog.v("UpdateLocation Started\n");	
	}

	public void Stop(Context context) {
		Intent intent = new Intent(context, UpdateLocation.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);		
        NetLog.v("UpdateLocation Stopped...\n");
	}
}