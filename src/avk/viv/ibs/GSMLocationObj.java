package avk.viv.ibs;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

public class GSMLocationObj extends LocationObj implements ILocationObj {

	public int cid;
	public int lac;
	public int mcc;
	public int mnc;
	public String sTime;
	public String sName;
   	
    GSMLocationObj(String beaconID,Context context,String sStatus) {
		
    	this.beaconID = beaconID;
    	this.statusText  = sStatus;
    	this.providerType = GatewayUtil.kGSM;
		TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
	    GsmCellLocation cellLocation = (GsmCellLocation)telephonyManager.getCellLocation();
    	String sOperator = telephonyManager.getNetworkOperator();
	    this.sName = telephonyManager.getNetworkOperatorName();
	    
    	this.cid = cellLocation.getCid();
        this.lac = cellLocation.getLac();
        try {
	        this.mcc = Integer.parseInt(sOperator.substring(0, 3));
	        this.mnc = Integer.parseInt(sOperator.substring(3));
        }
        catch (NumberFormatException e) 
        {
      	  this.mcc=0;
      	  this.mnc=0;
        };

        Date currentTime = new Date();
		SimpleDateFormat df = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
		sTime = df.format(currentTime);
   	};
   	
	public String getURL() {
		String sRequest = "//shluz.tygdenakarte.ru:60080/cgi-bin/Location_02?document=<request><function><name>PHONEFUNC_PKG.saveLocation_Phone_GSM</name><index>1</index><param>%s^%d^%d^%d^%d^-%s^%s^%s</param></function></request>";
	    return String.format(sRequest, beaconID,cid,lac,mcc,mnc,GatewayUtil.md5(GatewayUtil.deviceID),statusText,sTime);
	}
    
	public String getFile() {
		return "";
	}	
}; // GSMLocationObj
