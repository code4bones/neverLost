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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;



public class StatusActivity extends Activity {

	 public TextView lbLatitudeValue;
	 public TextView lbLongitudeValue;
	 public TextView lbAccuracyValue;
	 public TextView lbTimeValue;
	 public TextView lbUpdatesValue;
	 public TextView lbStatus;
	 public EditText txtStatus;
	 public Button   btnShowGSM;
	 public static LocationObj lastLocation = null;
	 public static UpdateHandler updateHandler = null;
	 
	 public class UpdateHandler extends Handler {
		 @Override
		 public void handleMessage(android.os.Message msg) {
			 final LocationObj locationObj = (LocationObj)msg.obj;
				runOnUiThread( new Runnable() {
					public void run() {
						GPSOrNetworkLocationObj locObj = (GPSOrNetworkLocationObj)(locationObj);
						
						SimpleDateFormat df = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
						String sStatus = "Статус / ";
						sStatus = sStatus.concat(locationObj.providerType == GatewayUtil.kGPS?"GPS":"WiFi");
						
						lbStatus.setText(sStatus);
						lbLatitudeValue.setText(String.valueOf(locObj.location.getLatitude()));
						lbLongitudeValue.setText(String.valueOf(locObj.location.getLongitude()));
						lbAccuracyValue.setText(String.valueOf(locObj.location.getAccuracy()));
						lbTimeValue.setText(String.valueOf(df.format(new Date(locObj.location.getTime()))));
						lbUpdatesValue.setText(String.valueOf(locObj.updateCount));
						
						
						NetLog.v("UpdateStatus %s\n",sStatus);
					}
				  });
		 }
	 };
	 
	 public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.statistics);
	        
	        
	        
	        lbStatus = (TextView)findViewById(R.id.lbStatus);
	        lbLatitudeValue = (TextView)findViewById(R.id.lbLatitudeValue);
	        lbLongitudeValue = (TextView)findViewById(R.id.lbLongitudeValue);
	        lbAccuracyValue = (TextView)findViewById(R.id.lbAccuracyValue);
	        lbTimeValue = (TextView)findViewById(R.id.lbTimeValue);
	        lbUpdatesValue = (TextView)findViewById(R.id.lbUpdatesValue);
	        txtStatus = (EditText)findViewById(R.id.txtStatus);
	        btnShowGSM = (Button)findViewById(R.id.btnShowGSM);
	        
	        SharedPreferences prefs = this.getSharedPreferences("prefs", 1);
	        txtStatus.setText(prefs.getString("statusText", ""));
	        txtStatus.setHint("Стаус на карте");

	        StatusActivity.updateHandler = new UpdateHandler();
	        
	        btnShowGSM.setOnClickListener( new View.OnClickListener() {
				
				public void onClick(View v) {
		        	Intent myIntent2 = new Intent(StatusActivity.this, GsmStatusActivity.class);
	                startActivityForResult(myIntent2, 0);
				}
			});
	        
	        txtStatus.setOnKeyListener( new OnKeyListener() {

				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					
						String sText = txtStatus.getText().toString();
						SharedPreferences prefs = StatusActivity.this.getSharedPreferences("prefs", 1);
						SharedPreferences.Editor edit = prefs.edit();
						edit.putString("statusText", sText);
						edit.commit();
						NetLog.MsgBox(StatusActivity.this,"Статус сохранен...",sText);
						return true;
					} // if
					return false;
				} // onKey
	        }); // onKeyListener
	        
	        NetLog.v("Status Handler Created...\n");	
	        
	    }

		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
		    MenuInflater inflater = getMenuInflater();
		    inflater.inflate(R.menu.status_menu, menu);
		    return true;
		}	
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
		    // Handle item selection
		    switch (item.getItemId()) {
		        case R.id.miSwitch:
		        	Intent myIntent = new Intent(this, GsmStatusActivity.class);
	                startActivityForResult(myIntent, 0);
		            return true;
		        default:
		            return super.onOptionsItemSelected(item);
		    }
		}
	 
	   @Override
		protected void onDestroy() {
			super.onDestroy();
		}
		
}
