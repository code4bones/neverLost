package avk.viv.abs;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.*;
import android.util.Log;
import android.location.*;
import android.net.*;
import android.widget.*;

public class NeverLostActivity extends Activity {
    /** Called when the activity is first created. */
    public StateListener stateListener = null;
	public static GatewayUtil gatewayUtil = null;
    public TextView tv1;
	public TextView tv2;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        gatewayUtil = new GatewayUtil(this,"mydevice");
       // gatewayUtil.sendRequest("<local>1234</local>");
       //gatewayUtil.Authorization("bada", "bada", null); 
       // gatewayUtil.getBeaconList("bada", "bada");
        BeaconObj obj = gatewayUtil.getLastBeaconLocation("1423");
        if ( obj != null )
         Log.v("clinch","Beacon = " + obj.toString());
        else Log.v("clinch","Error:"  + gatewayUtil.responseMSG);
        gatewayUtil.saveLocation("1423", obj.longitude, obj.latitude,obj.accuracy,"Hello");
        
        PackageInfo pinfo;
		try {
			pinfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
	        String s = new Integer(pinfo.versionCode).toString();
	        
	        tv1 = (TextView)findViewById(R.id.textView1);
	        tv2 = (TextView)findViewById(R.id.textView2);
	        
	        tv1.setText(s);
	        tv2.setText(pinfo.versionName);
	        
	        
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        stateListener = new StateListener(this) {
        	@Override
        	public void handleMessage(Message msg) {
        		Log.v("clinch",(String)msg.obj);
        	}
        };
        
	}
}

