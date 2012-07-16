package avk.viv.ibs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;



public class StatusActivity extends Activity implements IUpdateStatusUI<GPSOrNetworkLocationObj> {

	 public TextView lbLatitudeValue;
	 public TextView lbLongitudeValue;
	 public TextView lbAccuracyValue;
	 public TextView lbTimeValue;
	 public TextView lbStatus;
	 public Button   btnShowGSM;
	 public Button   btnGoogle;
	 public static GPSOrNetworkLocationObj lastLocation = null;
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
			lbStatus.setText(sStatus);
	 }
	 
	 public StatusActivity() {
	 }
	 
	 
	 public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.statistics);
	        
	        	        
	        lbStatus = (TextView)findViewById(R.id.lbStatus);
	        lbLatitudeValue = (TextView)findViewById(R.id.lbLatitudeValue);
	        lbLongitudeValue = (TextView)findViewById(R.id.lbLongitudeValue);
	        lbAccuracyValue = (TextView)findViewById(R.id.lbAccuracyValue);
	        lbTimeValue = (TextView)findViewById(R.id.lbTimeValue);
	        btnShowGSM = (Button)findViewById(R.id.btnShowGSM);
	        btnGoogle  = (Button)findViewById(R.id.btnGoogle);
	        
	        StatusActivity.updateHandler = new UpdateStatusHandler<StatusActivity,GPSOrNetworkLocationObj>(this);

	        btnShowGSM.setOnClickListener( new View.OnClickListener() {
				public void onClick(View v) {
		        	Intent myIntent2 = new Intent(StatusActivity.this, GsmStatusActivity.class);
	                startActivityForResult(myIntent2, 0);
				}
			});
	        
	        btnGoogle.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					String sURL = String.format("https://maps.google.ru/maps?f=q&source=s_q&hl=ru&q=%f,%f", lastLocation.location.getLatitude(),lastLocation.location.getLongitude());
				    Intent i = new Intent(Intent.ACTION_VIEW,Uri.parse(sURL));
	                startActivityForResult(i, 0);
				}
			});

	        SharedPreferences prefs = this.getSharedPreferences("prefs", 1);
    		final String sStatus  = prefs.getString("statusText", "");
        	final String beaconID = prefs.getString("beaconID", "0");
        	
        	// сначала попробуем получить из базы последнюю позицию
        	// если не получица, то дернем ласт-ноун
        	BackgroundTask<BeaconObj,String> task = new BackgroundTask<BeaconObj,String>(this) {
        		@Override
        		protected BeaconObj doInBackground(String ... args) {
        			GatewayUtil gw = new GatewayUtil(context);
        			return gw.getLastBeaconLocation(args[0]);
        		}
        		
        		public void onComplete(BeaconObj obj) {
        			// Если получится стащить из базы
        			if ( obj != null ) {
        				Location loc = new Location("gps");
        				loc.setLatitude(obj.latitude);
        				loc.setLongitude(obj.longitude);
        				loc.setAccuracy(obj.accuracy.floatValue());
        				SimpleDateFormat df = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        				try {
        					Date dt = df.parse(obj.date);
	        				long tm = dt.getTime();
	        				loc.setTime(tm);
	        				NetLog.Toast(StatusActivity.this,"Последняя локация из базы..." );
	    	        		updateUI(new GPSOrNetworkLocationObj(beaconID,loc,sStatus));
        				} catch ( ParseException e )
        				{
        					NetLog.MsgBox(StatusActivity.this,"Ошибка", "Не могу преобразовать:%s",e.toString());
        				} // try
        			} // beacon found
        			else {
        				// в базе нихера нет, тогда берем последнюю...
        				NetLog.Toast(StatusActivity.this,"Последняя актуальная локация..." );
        				StatusActivity.this.getLastKnownLocation(beaconID,sStatus);
        			} // beacon not found
        		} // onComplete
        	}; // BackgroundTask
        	task.execute(beaconID);
	 	}
	 
	 	public void getLastKnownLocation(String beaconID,String sStatus) {
	 		LocationManager lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        	String bestProvider = LocationObj.getBestProvider(lm);
   		 	Location location = lm.getLastKnownLocation(bestProvider);
        	// best is bastard !
   		 	if ( location == null ) {
        		String oldProvider = bestProvider;
   		 		if ( bestProvider.equals(LocationManager.GPS_PROVIDER )) bestProvider = LocationManager.NETWORK_PROVIDER;
        		else bestProvider = LocationManager.NETWORK_PROVIDER;
        		location = lm.getLastKnownLocation(bestProvider);
   		 		NetLog.Toast(this,"%s не доступен, использую %s", oldProvider,bestProvider);
        	}
   		 	
        	if ( location != null  ) {
        		NetLog.Toast(this, "Координаты/%s", bestProvider);
        		updateUI(new GPSOrNetworkLocationObj(beaconID,location,sStatus));
        	} else {
        		NetLog.Toast(this, "Ошибка получения координат через %s",bestProvider);
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
