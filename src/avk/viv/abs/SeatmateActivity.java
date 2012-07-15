package avk.viv.abs;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;


public class SeatmateActivity extends MapActivity {

	public MapView mapView = null;
	public ListView listView = null;
	public Button btnDone = null;
	public BeaconObj currentBeacon = null;
	
	
	public class SimpleOverlay extends Overlay {
		
		GeoPoint point;
		
		SimpleOverlay(GeoPoint point) {
			this.point = point;
		}
		
		@Override
		public boolean draw(Canvas canvas, MapView mapView,boolean shadow, long when) {
			super.draw(canvas, mapView, shadow);
			//---translate the GeoPoint to screen pixels---
			Point screenPts = new Point();
			mapView.getProjection().toPixels(this.point, screenPts);
			//---add the marker---
			Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.pin);   //pin.png image will require.
			canvas.drawBitmap(bmp, screenPts.x, screenPts.y-50, null);
			return true;
		}		
	};
	
	// По сути можно и отказаться от фона, ну да черт с ним...
	public class PositioningTask extends AsyncTask<Void,Void,BeaconObj> {
		
		public ProgressDialog progress = null;
		public Context context = null;
		public BeaconObj sourceBeacon = null;
	
		public PositioningTask(Context context,BeaconObj sourceBeacon) {
			this.context = context;
			this.sourceBeacon = sourceBeacon;
		}
		
		public void onComplete(BeaconObj beaconObj) {
			
		}
		
		@Override
		protected void onPreExecute() {
		    progress = new ProgressDialog(context);
		    progress.setTitle("Подождите...");
		    progress.setMessage("Получение данных...");
		    progress.setIndeterminate(true);
		    progress.setCancelable(true);
		    progress.show();
		  }
		
		@Override
		protected BeaconObj doInBackground(Void ... arg0) {
			GatewayUtil gw = new GatewayUtil(context);
			return gw.getLastBeaconLocation(sourceBeacon.uid);
		}
		
		protected void onPostExecute(BeaconObj beaconObj) {
			progress.dismiss();
			onComplete(beaconObj);
	  }	
	}; // PositioningTask
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.seatmate);
	    
	    mapView = (MapView)findViewById(R.id.mapview);
	    mapView.setBuiltInZoomControls(true);
	   
	}	
	
	public void selectBeacon() {
	    final GatewayUtil gw = new GatewayUtil(this);
	    final SharedPreferences prefs = this.getSharedPreferences("prefs", 1);
	    final Dialog dlg = new Dialog(this);
	    String sTitle = "";

	    dlg.setContentView(R.layout.select_beacon);
	    btnDone  = (Button)dlg.findViewById(R.id.btnDone);
	    listView = (ListView)dlg.findViewById(R.id.lbList);
		
		if ( currentBeacon != null )
			sTitle = String.format("Где на карте - %s",currentBeacon.name);
		else 
			sTitle = "Где друг на карте ?";

		dlg.setTitle(Html.fromHtml(sTitle));
		this.setTitle(Html.fromHtml(sTitle));
		

	    // установка дружбана для показа на карте
	    listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> item, View arg1, int position,long id) {
				BeaconObj obj = (BeaconObj)item.getAdapter().getItem(position);
				currentBeacon = obj;
				String sTitle = String.format("Где на карте - <b color='red'>%s<b>",currentBeacon.name);
				dlg.setTitle(Html.fromHtml(sTitle));
				SeatmateActivity.this.setTitle(Html.fromHtml(sTitle));
			}
	    });
	    
	    
	    // Список дружбанов нашего бикончика
		FetchBeaconsTask task = new FetchBeaconsTask(gw,prefs.getString("login", ""),
													    prefs.getString("password", ""),
													    prefs.getString("beaconID", ""),true) {
			@Override
			public void OnComplete(ArrayList<BeaconObj> beaconList) {
				if ( beaconList == null ) {
					NetLog.MsgBox(SeatmateActivity.this,"Ошибка","Ошибка получения списка:%s",gw.responseMSG);
			        return;
				} // beacon not set
				BeaconObj[] beacons = beaconList.toArray(new BeaconObj[beaconList.size()]);
				BeaconArrayAdapter ad = new BeaconArrayAdapter(SeatmateActivity.this, android.R.layout.simple_list_item_single_choice, beacons,Color.WHITE);
				listView.setAdapter(ad);
				dlg.show();
			} // onComplete
		};
		task.execute((Void[])null);
		
	    // показать его на карте
	    btnDone.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				if ( currentBeacon == null ) 
					NetLog.Toast(SeatmateActivity.this,"Пользователь не выбран...");
				else {
					NetLog.Toast(SeatmateActivity.this,"Где на карте %s",currentBeacon.name);
				}
				dlg.dismiss();
				getSeatmateLocation();
			}
		});
		
	}
	
	public void getSeatmateLocation() {
		
		PositioningTask task = new PositioningTask(this,currentBeacon) {
			public void onComplete(BeaconObj beaconObj) {
				if ( beaconObj != null ) {
					NetLog.MsgBox(SeatmateActivity.this, "111","%s",beaconObj.toString() );
					showLocation(beaconObj);
				}
				else
					NetLog.MsgBox(SeatmateActivity.this, "Внимание","У активного телефона нет друзей...");
			}
		};
		task.execute((Void[])null);
	}
	
	public void showLocation(BeaconObj beaconObj) {
		MapController mapCtrl = mapView.getController();
		int lat = (int)(beaconObj.latitude * 1E6);
		int lng = (int)(beaconObj.longitude *1E6);
		GeoPoint pt = new GeoPoint(lat, lng);
		mapCtrl.animateTo(pt);
		mapCtrl.setCenter(pt);
		mapCtrl.setZoom(20);
			
		List<Overlay> list = mapView.getOverlays();
		list.clear();
		list.add(new SimpleOverlay(pt));
		mapView.invalidate();
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.map_menu, menu);
	    return true;
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.miSelectBeacon:
	            selectBeacon();
	        	return true;
	        case R.id.miRefresh:
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	
}
