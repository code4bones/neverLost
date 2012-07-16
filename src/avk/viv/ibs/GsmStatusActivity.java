package avk.viv.ibs;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class GsmStatusActivity extends Activity implements IUpdateStatusUI<GSMLocationObj> {

	 public static UpdateStatusHandler<GsmStatusActivity,GSMLocationObj> updateHandler = null;
	 public TextView lbCidValue;
	 public TextView lbLacValue;
	 public TextView lbMccValue;
	 public TextView lbNameValue;
	 public TextView lbTimeValue;
	 public Button	 btnGoogle;
	 
	 public void updateUI(GSMLocationObj locObj) {
			
			lbCidValue.setText(String.valueOf(locObj.cid));
			lbLacValue.setText(String.valueOf(locObj.lac));
			lbMccValue.setText(String.format("%d/%d", locObj.mcc,locObj.mnc));
			lbNameValue.setText(locObj.sName);
			lbNameValue.setText(locObj.sName);
			lbTimeValue.setText(locObj.sTime);
			
			NetLog.v("GSM UpdateStatus %s\n",locObj.sTime);
	 }
	 
	 public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.gsm_status);
	        
	        lbCidValue = (TextView)findViewById(R.id.lbCidValue);
	        lbLacValue = (TextView)findViewById(R.id.lbLacValue);
	        lbMccValue = (TextView)findViewById(R.id.lbMccValue);
	        lbNameValue= (TextView)findViewById(R.id.lbNameValue);
	        lbTimeValue= (TextView)findViewById(R.id.lbTimeValue);
	        btnGoogle  = (Button)findViewById(R.id.btnGoogle);
	        GsmStatusActivity.updateHandler = new UpdateStatusHandler<GsmStatusActivity,GSMLocationObj>(this);

        	SharedPreferences prefs = this.getSharedPreferences("prefs", 1);
        	updateUI(new GSMLocationObj(prefs.getString("beaconID", ""),this,prefs.getString("statusText", "")));
	 
        	btnGoogle.setOnClickListener( new View.OnClickListener() {
				public void onClick(View v) {
					try {
						NetLog.v("GSM MAP\n");
						if ( !displayMap(0,0) )
							NetLog.Toast(GsmStatusActivity.this, "Ошибка преобразования GSM координат...");
					} catch (Exception e) {
						NetLog.v("MAP => %s\n",e.toString());
						e.printStackTrace();
					}
					
				}
			});
	 }	
	 
	 public boolean displayMap(int cellID, int lac) throws Exception
	 {
	 String urlString = "http://www.google.com/glm/mmap";
	 //---open a connection to Google Maps API---
	 URL url = new URL(urlString);
	 URLConnection conn = url.openConnection();
	 HttpURLConnection httpConn = (HttpURLConnection) conn;
	 httpConn.setRequestMethod("POST");
	 httpConn.setDoOutput(true);
	 httpConn.setDoInput(true);
	 httpConn.connect();
	 //---write some custom data to Google Maps API---
	 OutputStream outputStream = httpConn.getOutputStream();
	 WriteData(outputStream, cellID, lac);
	 //---get the response---
	 InputStream inputStream = httpConn.getInputStream();
	 DataInputStream dataInputStream = new DataInputStream(inputStream);
	 //---interpret the response obtained---
	 dataInputStream.readShort();
	 dataInputStream.readByte();
	 int code = dataInputStream.readInt();
	 if (code == 0) {
		 double lat = (double) dataInputStream.readInt() / 1000000D;
		 double lng = (double) dataInputStream.readInt() / 1000000D;
		 dataInputStream.readInt();
		 dataInputStream.readInt();
		 dataInputStream.readUTF();
		 //---display Google Maps---
		 String uriString = "geo:" + lat
		 + "," + lng;
		 Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
		 Uri.parse(uriString));
		 startActivity(intent);
		 return true;
	 }
	 else
		 return false;
	 }
	 
	 private void WriteData(OutputStream out, int cellID, int lac)
			 throws IOException
			 {
			 DataOutputStream dataOutputStream = new DataOutputStream(out);
			 dataOutputStream.writeShort(21);
			 dataOutputStream.writeLong(0);
			 dataOutputStream.writeUTF("en");
			 dataOutputStream.writeUTF("Android");
			 dataOutputStream.writeUTF("1.0");
			 dataOutputStream.writeUTF("Web");
			 dataOutputStream.writeByte(27);
			 dataOutputStream.writeInt(0);
			 dataOutputStream.writeInt(0);
			 dataOutputStream.writeInt(3);
			 dataOutputStream.writeUTF("");
			 dataOutputStream.writeInt(cellID);
			 dataOutputStream.writeInt(lac);
			 dataOutputStream.writeInt(0);
			 dataOutputStream.writeInt(0);
			 dataOutputStream.writeInt(0);
			 dataOutputStream.writeInt(0);
			 dataOutputStream.flush();
			 }	 
}// class
