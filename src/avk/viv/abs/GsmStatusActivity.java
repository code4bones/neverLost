package avk.viv.abs;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;


public class GsmStatusActivity extends Activity {

	 public static GSMUpdateHandler updateHandler = null;
	 public TextView lbCidValue;
	 public TextView lbLacValue;
	 public TextView lbMccValue;
	 public TextView lbNameValue;
	 public TextView lbTimeValue;
	 
	 
	 public class GSMUpdateHandler extends Handler {
		 @Override
		 public void handleMessage(Message msg) {
			 final LocationObj locationObj = (LocationObj)msg.obj;
				runOnUiThread( new Runnable() {
					public void run() {
						GSMLocationObj locObj = (GSMLocationObj)locationObj;
							
						lbCidValue.setText(String.valueOf(locObj.cid));
						lbLacValue.setText(String.valueOf(locObj.lac));
						lbMccValue.setText(String.format("%d/%d", locObj.mcc,locObj.mnc));
						lbNameValue.setText(locObj.sName);
						lbNameValue.setText(locObj.sName);
						lbTimeValue.setText(locObj.sTime);
						
						NetLog.v("GSM UpdateStatus %s\n",locObj.sTime);
					}
				  });
		 }
	 }; // class Update Handler

	 
	 public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.gsm_status);
	        
	        lbCidValue = (TextView)findViewById(R.id.lbCidValue);
	        lbLacValue = (TextView)findViewById(R.id.lbLacValue);
	        lbMccValue = (TextView)findViewById(R.id.lbMccValue);
	        lbNameValue= (TextView)findViewById(R.id.lbNameValue);
	        lbTimeValue= (TextView)findViewById(R.id.lbTimeValue);
	        GsmStatusActivity.updateHandler = new GSMUpdateHandler();
	 }	 
}
