package avk.viv.abs;

import android.app.Activity;
import android.os.Handler;



public class UpdateStatusHandler<_Activity,_LocationObj> extends Handler {

	public _Activity activity = null;
	public _LocationObj locationObj;
	
	public UpdateStatusHandler(_Activity ac) {
		this.activity = ac;
	}	

	@SuppressWarnings("unchecked")
	@Override
	 public void handleMessage(android.os.Message msg) {
		locationObj = (_LocationObj)msg.obj;
		((Activity)activity).runOnUiThread( new Runnable() {
			public void run() {
				((IUpdateStatusUI<_LocationObj>)activity).updateUI(locationObj);
			}
		  });
	 }
	
};
