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
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
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



public class StatusActivity extends Activity implements IUpdateStatusUI<GPSOrNetworkLocationObj> {

	 public TextView lbLatitudeValue;
	 public TextView lbLongitudeValue;
	 public TextView lbAccuracyValue;
	 public TextView lbTimeValue;
	 public TextView lbUpdatesValue;
	 public TextView lbStatus;
	 public Button   btnShowGSM;
	 public static LocationObj lastLocation = null;
	 public static UpdateStatusHandler<StatusActivity,GPSOrNetworkLocationObj> updateHandler = null;
	 
	 
	 public void updateUI(GPSOrNetworkLocationObj locObj) {

		 	lastLocation = locObj;
			SimpleDateFormat df = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
			String sStatus = "Статус / ";
			sStatus = sStatus.concat(locObj.providerType == GatewayUtil.kGPS?"GPS":"WiFi");
			
			lbLatitudeValue.setText(String.valueOf(locObj.location.getLatitude()));
			lbLongitudeValue.setText(String.valueOf(locObj.location.getLongitude()));
			lbAccuracyValue.setText(String.valueOf(locObj.location.getAccuracy()));
			lbTimeValue.setText(String.valueOf(df.format(new Date(locObj.location.getTime()))));
			lbUpdatesValue.setText(String.valueOf(locObj.updateCount));
			lbStatus.setText(sStatus);
	 }
	 
	 public StatusActivity() {
		 NetLog.v("STATUS ACTIVITY\n");
	 }
	 
	 /*
	public void getDirections(float latitude, float longitude) {
	    launchIntent(new Intent(Intent.ACTION_VIEW,
	        Uri.parse("http://maps.google.com/maps?f=d&daddr=" + latitude + "," + longitude)));
  	}	  
  	*/
	 
	 public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.statistics);
	        
	        	        
	        lbStatus = (TextView)findViewById(R.id.lbStatus);
	        lbLatitudeValue = (TextView)findViewById(R.id.lbLatitudeValue);
	        lbLongitudeValue = (TextView)findViewById(R.id.lbLongitudeValue);
	        lbAccuracyValue = (TextView)findViewById(R.id.lbAccuracyValue);
	        lbTimeValue = (TextView)findViewById(R.id.lbTimeValue);
	        lbUpdatesValue = (TextView)findViewById(R.id.lbUpdatesValue);
	        btnShowGSM = (Button)findViewById(R.id.btnShowGSM);
	        
	        StatusActivity.updateHandler = new UpdateStatusHandler<StatusActivity,GPSOrNetworkLocationObj>(this);
	        NetLog.v("Status Handler Created...\n");	

	        btnShowGSM.setOnClickListener( new View.OnClickListener() {
				public void onClick(View v) {
		        	Intent myIntent2 = new Intent(StatusActivity.this, GsmStatusActivity.class);
	                startActivityForResult(myIntent2, 0);
				}
			});
	        
	        if ( lastLocation != null ) {
	        	updateUI((GPSOrNetworkLocationObj)lastLocation);
	        	NetLog.v("StatusActivity - using last location\n");
	        } else {
	        	LocationManager lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
	        	Criteria criteria = new Criteria();
	        	criteria.setAccuracy( Criteria.ACCURACY_FINE);
	        	criteria.setAltitudeRequired( false );
	        	criteria.setBearingRequired( false );
	        	criteria.setCostAllowed( true );
	        	criteria.setPowerRequirement( Criteria.POWER_LOW);
	        	String provider = lm.getBestProvider( criteria, true);	        	
	        	Location location = lm.getLastKnownLocation(provider);

	        	SharedPreferences prefs = this.getSharedPreferences("prefs", 1);
	        	updateUI(new GPSOrNetworkLocationObj(prefs.getString("beaconID", ""),location,prefs.getString("statusText", "")));
	        }
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
