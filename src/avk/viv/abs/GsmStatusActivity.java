package avk.viv.abs;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;


public class GsmStatusActivity extends Activity implements IUpdateStatusUI<GSMLocationObj> {

	 public static UpdateStatusHandler<GsmStatusActivity,GSMLocationObj> updateHandler = null;
	 public TextView lbCidValue;
	 public TextView lbLacValue;
	 public TextView lbMccValue;
	 public TextView lbNameValue;
	 public TextView lbTimeValue;
	 
	 
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
	        GsmStatusActivity.updateHandler = new UpdateStatusHandler<GsmStatusActivity,GSMLocationObj>(this);

        	SharedPreferences prefs = this.getSharedPreferences("prefs", 1);
        	updateUI(new GSMLocationObj(prefs.getString("beaconID", ""),this,prefs.getString("statusText", "")));
	 }	 
}
