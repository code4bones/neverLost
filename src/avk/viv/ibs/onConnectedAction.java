package avk.viv.ibs;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;


public class onConnectedAction extends AsyncTask<Void,Void,Boolean> {

	public ProgressDialog progress;
	public Context context;
	public String  sTitle;
	public String  sMessage;
	
	onConnectedAction(Context context,String sTitle,String fmt,Object ... args) {
		this.context = context;
		this.sTitle = sTitle;
		this.sMessage = String.format(fmt, args);
	}
	
	public void onSuccess() {
		
	}
	
	public void onFail() {
		NetLog.MsgBox(context, "Ошибка сети", "Нет доступа к шлюзу");
	}
	
	@Override
	protected void onPreExecute() {
	    progress = new ProgressDialog(context);
	    progress.setTitle(sTitle);
	    progress.setMessage(sMessage);
	    progress.setIndeterminate(true);
	    progress.setCancelable(true);
	    progress.show();
	  }
	
	@Override
	protected Boolean doInBackground(Void ... arg0) {
		return GatewayUtil.isConnected(context);
	}
	
	protected void onPostExecute(Boolean connected) {
		NetLog.v("CONNECTED ? %s",connected.toString());
		progress.dismiss();
        if ( connected ) onSuccess();
        else onFail();
  }	
}
