package avk.viv.ibs;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;


public class BackgroundTask<Result,Param> extends AsyncTask<Param, Void, Result> {

	public ProgressDialog progress = null;
	public Context context = null;
	//public Result sourceBeacon = null;

	public BackgroundTask(Context context) {
		this.context = context;
		//this.sourceBeacon = sourceBeacon;
	}
	
	public void onComplete(Result result) {
		
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
	protected Result doInBackground(Param ... arg0) {
		return null; 
	}
	
	protected void onPostExecute(Result result) {
		progress.dismiss();
		onComplete(result);
  }	
	
	public void exec() {
		this.execute((Param[])null);
	}
};  // class
