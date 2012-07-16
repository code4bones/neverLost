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
import android.graphics.Paint;
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

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;


public class SeatmateActivity extends MapActivity {

	public MapView mapView = null;
	public ListView listView = null;
	public Button btnDone = null;
	public BeaconObj currentBeacon = null;
	
	public class CircleOverlay extends Overlay {

	    Context context;
	    double mLat;
	    double mLon;
	    float mRadius;

	     public CircleOverlay(Context _context, double _lat, double _lon, float radius ) {
	            context = _context;
	            mLat = _lat;
	            mLon = _lon;
	            mRadius = radius;
	     }

	     public void draw(Canvas canvas, MapView mapView, boolean shadow) {
	         super.draw(canvas, mapView, shadow); 

	         if(shadow) return; // Ignore the shadow layer

	         Projection projection = mapView.getProjection();

	         Point pt = new Point();

	         GeoPoint geo = new GeoPoint((int) (mLat *1e6), (int)(mLon * 1e6));

	         projection.toPixels(geo ,pt);
	         float circleRadius = projection.metersToEquatorPixels(mRadius);

	         Paint innerCirclePaint;

	         innerCirclePaint = new Paint();
	         innerCirclePaint.setColor(Color.BLUE);
	         innerCirclePaint.setAlpha(25);
	         innerCirclePaint.setAntiAlias(true);

	         innerCirclePaint.setStyle(Paint.Style.FILL);

	         canvas.drawCircle((float)pt.x, (float)pt.y, circleRadius, innerCirclePaint);
			Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.pin);   //pin.png image will require.
			canvas.drawBitmap(bmp, pt.x-7, pt.y-28, null);

	     }
	}
	
	public class SimpleOverlay extends Overlay {
		
		GeoPoint point;
		
		SimpleOverlay(GeoPoint point) {
			this.point = point;
		}
		
		@Override
		public boolean draw(Canvas canvas, MapView mapView,boolean shadow, long when) {
			super.draw(canvas, mapView, shadow);
			Point screenPts = new Point();
			mapView.getProjection().toPixels(this.point, screenPts);
			Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.pin);   //pin.png image will require.
			canvas.drawBitmap(bmp, screenPts.x, screenPts.y-50, null);
			return true;
		}		
	};
	
	// �� ���� ����� � ���������� �� ����, �� �� ���� � ���...
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
		    progress.setTitle("���������...");
		    progress.setMessage("��������� ������...");
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
	   
	    selectBeacon();
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
			sTitle = String.format("��� �� ����� - %s",currentBeacon.name);
		else 
			sTitle = "��� �� �� ����� ?";

		dlg.setTitle(Html.fromHtml(sTitle));
		this.setTitle(Html.fromHtml(sTitle));
		

	    // ��������� �������� ��� ������ �� �����
	    listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> item, View arg1, int position,long id) {
				BeaconObj obj = (BeaconObj)item.getAdapter().getItem(position);
				currentBeacon = obj;
				String sTitle = String.format("��� �� ����� - <b>%s<b>",currentBeacon.name);
				dlg.setTitle(Html.fromHtml(sTitle));
				SeatmateActivity.this.setTitle(Html.fromHtml(sTitle));
			}
	    });
	    
	    final String sLogin = prefs.getString("login", "");
	    final String sPassword = prefs.getString("password", "");
	    final String sBeaconID = prefs.getString("beaconID", "");
	    final String sBeaconName = prefs.getString("beaconName","");
	    		
	    
	    // ������ ��������� ������ ���������
		FetchBeaconsTask task = new FetchBeaconsTask(gw,sLogin,sPassword,sBeaconID,false) {
			@Override
			public void OnComplete(ArrayList<BeaconObj> beaconList) {
				if ( beaconList == null ) {
					NetLog.MsgBox(SeatmateActivity.this,"������","������ ��������� ������:%s",gw.responseMSG);
			        dlg.dismiss();
					return;
				} // beacon not set
				if ( beaconList.size() == 0 ) {
					NetLog.MsgBox(SeatmateActivity.this,"��������","� %s ��� ������...",sBeaconName);
					dlg.dismiss();
					return;
				}
				BeaconObj[] beacons = beaconList.toArray(new BeaconObj[beaconList.size()]);
				BeaconArrayAdapter ad = new BeaconArrayAdapter(SeatmateActivity.this, android.R.layout.simple_list_item_single_choice, beacons,Color.WHITE);
				listView.setAdapter(ad);
				dlg.show();
			} // onComplete
		};
		task.execute((Void[])null);
		
	    // �������� ��� �� �����
	    btnDone.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				dlg.dismiss();
				if ( currentBeacon == null ) 
					NetLog.Toast(SeatmateActivity.this,"������������ �� ������...");
				else {
					NetLog.Toast(SeatmateActivity.this,"��� �� ����� %s",currentBeacon.name);
					getSeatmateLocation();
				}
			}
		});
		
	}
	
	public void getSeatmateLocation() {
		
		PositioningTask task = new PositioningTask(this,currentBeacon) {
			public void onComplete(BeaconObj beaconObj) {
				if ( beaconObj != null ) {
					showLocation(beaconObj);
				}
				else
					NetLog.MsgBox(SeatmateActivity.this, "��������","��� ������� � ����������� %s",currentBeacon.name);
			}
		};
		task.execute((Void[])null);
	}
	
	public void showLocation(BeaconObj beaconObj) {

		String sTitle = String.format("%s//%s (%s)",currentBeacon.name,beaconObj.date,beaconObj.status);
		this.setTitle(Html.fromHtml(sTitle));
		
		MapController mapCtrl = mapView.getController();
		int lat = (int)(beaconObj.latitude * 1E6);
		int lng = (int)(beaconObj.longitude *1E6);
		GeoPoint pt = new GeoPoint(lat, lng);
		mapCtrl.animateTo(pt);
		mapCtrl.setCenter(pt);
		mapCtrl.setZoom(18);
		
		
		List<Overlay> list = mapView.getOverlays();
		list.clear();
		list.add(new CircleOverlay(this,beaconObj.latitude,beaconObj.longitude,beaconObj.accuracy.floatValue()));
		
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
	        	getSeatmateLocation();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	
}
