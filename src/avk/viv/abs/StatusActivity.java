package avk.viv.abs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.EditText;
import android.widget.TextView;



public class StatusActivity extends Activity {

	 public TextView lbLatitudeValue;
	 public TextView lbLongitudeValue;
	 public TextView lbAccuracyValue;
	 public TextView lbTimeValue;
	 public TextView lbUpdatesValue;
	 public EditText txtStatus;
	 public LocationObj lastLocation;
	 public static UpdateHandler updateHandler = null;
	 
	 public class UpdateHandler extends Handler {
		 @Override
		 public void handleMessage(android.os.Message msg) {
				runOnUiThread( new Runnable() {
					public void run() {
						SimpleDateFormat df = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
						String sTime = df.format(new Date());
						lbTimeValue.setText(sTime);
						NetLog.v("UpdateStatus time = %s\n",sTime);
					}
				  });
		 }
	 };
	 
	 public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.statistics);

	        lbLatitudeValue = (TextView)findViewById(R.id.lbLatitudeValue);
	        lbLongitudeValue = (TextView)findViewById(R.id.lbLongitudeValue);
	        lbAccuracyValue = (TextView)findViewById(R.id.lbAccuracyValue);
	        lbTimeValue = (TextView)findViewById(R.id.lbTimeValue);
	        lbUpdatesValue = (TextView)findViewById(R.id.lbUpdatesValue);
	        txtStatus = (EditText)findViewById(R.id.txtStatus);
	        
	        txtStatus.setText("пусто");
	        txtStatus.setHint("Стаус на карте");

	        //if ( StatusActivity.updateHandler == null ) {
	        	StatusActivity.updateHandler = new UpdateHandler();
	        	NetLog.v("Status Handler Created...\n");	
	        //}
	    }

	   @Override
		protected void onDestroy() {
			super.onDestroy();
		}
		
}
