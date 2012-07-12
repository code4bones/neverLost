package avk.viv.abs;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;


public class FetchBeaconsTask extends AsyncTask<Void,Void,ArrayList<BeaconObj>> {

	private GatewayUtil gatewayUtil;
	private boolean	    fetchMates;
	private String		sLogin;
	private String	 	sPassword;
	private String		sBeaconID;
	private ProgressDialog progress;
	
	public FetchBeaconsTask(GatewayUtil gw,String sLogin,String sPassword,String beaconID,boolean fetchMates) {
		this.gatewayUtil = gw;
		this.fetchMates  = fetchMates;
		this.sLogin = sLogin;
		this.sPassword = sPassword;
		this.sBeaconID = beaconID;
	}
	
	@Override
	protected void onPreExecute() {
	    progress = new ProgressDialog(this.gatewayUtil.context);
	    progress.setMessage("Получение списка телефонов");
	    progress.setIndeterminate(true);
	    progress.setCancelable(true);
	    progress.show();
	  }
	
	public void OnComplete(ArrayList<BeaconObj> beacons) {
		Log.v("clinch","I've not have to be called");
	}
	
	@Override
	protected ArrayList<BeaconObj> doInBackground(Void... params) {
		ArrayList<BeaconObj> list = null;
	
    	if ( this.gatewayUtil.Authorization(sLogin, sPassword, null) == false ) {
         	//Toast.makeText(this.gatewayUtil.context, this.gatewayUtil.responseMSG,Toast.LENGTH_SHORT).show();
    		return null;
    	}
		
		if ( this.fetchMates ) list = this.gatewayUtil.getSeatMates(this.sBeaconID);
		else
			list = gatewayUtil.getBeaconList(this.sLogin, this.sPassword);
		return list;
	}

	protected void onPostExecute(ArrayList<BeaconObj> list) {
        progress.dismiss();
        OnComplete(list);
  }	
}
