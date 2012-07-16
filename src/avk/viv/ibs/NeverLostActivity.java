package avk.viv.ibs;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;

public class NeverLostActivity extends Activity {
    /** Called when the activity is first created. */
	public static GatewayUtil gatewayUtil = null;
    
	final public static int kSelect = 0;
	final public static int kActivate = 1;
	final public static int kDeactivate = 2;
	
	public int currentState = kSelect;
	
	// GUI
	public Button	bnAction;
	public TextView lbVersion;
	public EditText txtLogin;
	public EditText txtPassword;
	//public Spinner  spBeacons;
	//public TextView lbInterval;
	public EditText txtStatusText;
	//public ToggleButton tgActive;
	static public BeaconObj currentBeacon;
    
	public Intent statusIntent = null;
	static TrackerService trackerService = null;
	public ServiceConnection serviceConnection;
	public boolean isServiceBound;
	public boolean isRestoreState;
	public boolean isActive;
	public int nNextAction = kSelect;
			
	public static final PrintStream log = NetLog.Init("clinch","neverLost.log",true);
	
	String getActionTitle() {
		String[] caption = {"������� �������","������������","��������������"};
		currentState %= 3; // currentState % 3;
		return caption[currentState];
	}
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
		
		SharedPreferences prefs = this.getSharedPreferences("prefs", 1);
        isServiceBound = false;
        
        // GUI Initialization
        this.lbVersion = (TextView)findViewById(R.id.lbVersion);
        this.txtLogin = (EditText)findViewById(R.id.txtLogin);
        this.txtPassword = (EditText)findViewById(R.id.txtPassword);
        //this.lbInterval = (TextView)findViewById(R.id.lbInterval);
        //this.tgActive = (ToggleButton)findViewById(R.id.tgActive);
        //this.spBeacons = (Spinner)findViewById(R.id.spBeacons);
        this.bnAction = (Button)findViewById(R.id.sbFetchBeacons);
        this.txtStatusText = (EditText)findViewById(R.id.txtStatus);

        this.bnAction.setText(getActionTitle());
        
        // ���� �� ���������� - �����
        //this.tgActive.setEnabled(false);
        //this.spBeacons.setEnabled(false);
        //this.lbInterval.setTextColor(Color.DKGRAY);
        
        currentBeacon = new BeaconObj();
        isActive = false;
       
		currentBeacon.login    = "v.g.0873@bk.ru";
		currentBeacon.password = "9d8x4";
       
		// ��������� ������ ���������
		if ( prefs.getString("login", null) != null )
		{
			isRestoreState = true;
			NetLog.v("GUI: Saved settings are available...\n");
			//currentBeacon.login    = prefs.getString("login", "");
			//currentBeacon.password = prefs.getString("password", "");
			currentBeacon.uid      = prefs.getString("beaconID", "");
			currentBeacon.name     = prefs.getString("beaconName", "");
			currentBeacon.interval = prefs.getInt("interval", 10);
			currentBeacon.selectedBeaconIndex = prefs.getInt("selectedBeaconIndex", 0);
			currentBeacon.status = prefs.getString("statusText", "");
			NetLog.v("GUI: login = %s,password = %s,beaconID = %s,beaconName = %s interval = %d,idx = %d",
					currentBeacon.login,
					currentBeacon.password,
					currentBeacon.uid,
					currentBeacon.name,
					currentBeacon.interval,
					currentBeacon.selectedBeaconIndex);
		}
		
		this.txtLogin.setText(currentBeacon.login);
		this.txtPassword.setText(currentBeacon.password);
        this.txtStatusText.setText(currentBeacon.status);
        
		// HTTP Gateway Utility Object
        gatewayUtil = new GatewayUtil(this);
        
        // Setup Version information label
        PackageInfo pinfo;
		try {
			pinfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
	        this.lbVersion.setText(pinfo.versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		// ��������� ������ ��������� � ������������� ��������
		View.OnClickListener onPerformAction = new View.OnClickListener() {
			public void onClick(View v) {
				
				String sLogin = txtLogin.getText().toString();
				String sPassword = txtPassword.getText().toString();
				
				currentState++;
				String sTitle = NeverLostActivity.this.getActionTitle();
				NeverLostActivity.this.bnAction.setText(sTitle);

				/*
				if ( !validateLogin(sLogin,sPassword) )
					return;
				
				// ������� �����....
				FetchBeaconsTask task = new FetchBeaconsTask(NeverLostActivity.gatewayUtil,sLogin,sPassword,null,false) {
					@Override
					public void OnComplete(ArrayList<BeaconObj> beaconList) {
						if ( beaconList == null ) {
							NetLog.MsgBox(NeverLostActivity.this,"������","������ ��������� ������:%s",gatewayUtil.responseMSG);
							Toast.makeText(NeverLostActivity.this, gatewayUtil.responseMSG,Toast.LENGTH_SHORT).show();
					        //tgActive.setEnabled(false);
					        //spBeacons.setEnabled(false);
					        //lbInterval.setTextColor(Color.DKGRAY);
					        return;
						}
						BeaconObj[] beacons = beaconList.toArray(new BeaconObj[beaconList.size()]);
						BeaconArrayAdapter ad = new BeaconArrayAdapter(NeverLostActivity.this, android.R.layout.simple_spinner_item, beacons,Color.BLACK);
						ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						//spBeacons.setAdapter(ad);	
						//spBeacons.setPrompt("��������...");
						//tgActive.setEnabled(true);
				        //spBeacons.setEnabled(!isActive);
						// ���� �������������� �������� �� sharedprefs...
						//if ( isRestoreState )
							//spBeacons.setSelection(currentBeacon.selectedBeaconIndex);
					}
				};
				task.execute((Void[])null);
			}*/
		}; // OnClickListener

		// �������� �������� ������� �� ��������
		OnItemSelectedListener onSelectBeacon = new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> item,View view,int position,long id) {
				BeaconObj obj = (BeaconObj)item.getAdapter().getItem(position);
				currentBeacon = obj;
				// ��������, �� ������ ����� ��� ����-������������� ������, ����� �������� � ���������...
				currentBeacon.login = txtLogin.getText().toString();
				currentBeacon.password = txtPassword.getText().toString();
				currentBeacon.selectedBeaconIndex = position;
				currentBeacon.interval = NeverLostActivity.gatewayUtil.getFrequency(obj.uid);
				if ( currentBeacon.interval <= 0 ) {
					Toast.makeText(NeverLostActivity.this, gatewayUtil.responseMSG,Toast.LENGTH_SHORT).show();
					// 10 ��������� �� ���������
					currentBeacon.interval = 10;
				}
				
				//lbInterval.setText(String.format("������ ����������: %d ���.",currentBeacon.interval));
		        //lbInterval.setTextColor(Color.WHITE);
		        
		        // �������������� ����������
		        onConnectedAction task = new onConnectedAction(NeverLostActivity.this,"���������...","���� ����������� �������� %s",currentBeacon.name) {
		           // � ����� ���������� ��������� ���������
				    public void onSuccess() { 
		        	if ( !gatewayUtil.Authorization(currentBeacon.login,currentBeacon.password, currentBeacon.uid) ) 
				    	NetLog.MsgBox(NeverLostActivity.this, "���������", "�� �������� ������������ �������: %s", gatewayUtil.responseMSG);
				     else 
				    	NetLog.Toast(NeverLostActivity.this,"������� �����������: %s",currentBeacon.name);
				    	NeverLostActivity.this.saveBeacon();
				    } // onSuccess
				   }; // onConnectedActoin
		        task.execute((Void[])null);
			}
			public void onNothingSelected(AdapterView<?> arg) {
			}
		};
		
		
		// ��������� ������ ���� ������ � ������
		OnKeyListener onValidateInput = new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// validate login/password fields values 
				boolean fOk = false;
				if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                	EditText txtEdit = (EditText)v; 
                	String sText = txtEdit.getText().toString();
                	if ( sText.length() > 0 ) 
                		return fOk;
                	
                	fOk = true;
                	txtEdit.requestFocus();
                	if ( txtEdit == txtLogin ) 
                	 Toast.makeText(NeverLostActivity.this, "������� �����!",Toast.LENGTH_SHORT).show();
                	else if ( txtEdit == txtPassword )
                   	 Toast.makeText(NeverLostActivity.this, "������� ������!",Toast.LENGTH_SHORT).show();
                	// prevent from resign
                	return fOk;
                } // if
                return fOk;
			} // onKey
		}; // OnKeyListener
		
		
		// ���������/������ �������
		/*
		CompoundButton.OnCheckedChangeListener onChangeActive = new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton arg0, boolean fActive) {
				txtLogin.setEnabled(!fActive);
				txtLogin.setClickable(!fActive);
				txtPassword.setEnabled(!fActive);
				txtPassword.setClickable(!fActive);
				txtStatusText.setEnabled(!fActive);
				//bnFetchBeacons.setEnabled(!fActive);
				//spBeacons.setEnabled(!fActive);
				controlService(fActive);
			} */
		};
		
		
		serviceConnection = new ServiceConnection() {
			public void onServiceConnected(ComponentName className, IBinder binder) {
				 trackerService = ((TrackerService.TrackerBinder)binder).getService();
			}
			public void onServiceDisconnected(ComponentName arg0) {
				 trackerService = null;
			}
		};
		

		// ������ ������
		//this.tgActive.setOnCheckedChangeListener(onChangeActive);
		this.bnAction.setOnClickListener( onPerformAction );		
		//this.spBeacons.setOnItemSelectedListener( onSelectBeacon );
		//this.txtLogin.setOnKeyListener(onValidateInput);
		//this.txtPassword.setOnKeyListener(onValidateInput);

		// ��������� ����������
		if ( (this.isActive = isServiceRunning()) == true ) {
			NetLog.v("Service is already Running...");
			//tgActive.setChecked(true);
			//tgActive.setEnabled(true);
		}
		
		// ���� ���� ���������� ��������� - ��������������� �������� � ��������
		if ( isRestoreState ) { 
			onConnectedAction task = new onConnectedAction(this,"���������","���� �������� ����...") {
				public void onSuccess() {
						//bnFetchBeacons.performClick();
				} // onSuccess
			};// onConnectedAction
			task.execute((Void[])null);
		} // isRestoreState		
		
	} // onCreate

	boolean isServiceRunning() {
		String srvName = TrackerService.class.getName();
		ActivityManager mgr = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
		for ( RunningServiceInfo srv : mgr.getRunningServices(Integer.MAX_VALUE)) {
			if ( srvName.equals(srv.service.getClassName()))
				return true;
		}
		return false;
	}
	
	void doBindService() {
		if ( bindService(new Intent(this,TrackerService.class),serviceConnection,Context.BIND_AUTO_CREATE) ) {
			isServiceBound = true;
		} else {
			NetLog.MsgBox(this, "������","�� ������� �������� ������ � �������");
		}
	}
	
	void doUnbindService() {
		if ( isServiceBound ) {
			unbindService(serviceConnection);
			isServiceBound = false;
		}
	}
	
	boolean controlService(boolean fActive) {
		SharedPreferences prefs = NeverLostActivity.this.getSharedPreferences("prefs",1);
		SharedPreferences.Editor edit = prefs.edit();

		AlarmManager service = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
	    Intent intent = new Intent(this,StartTrackerServiceReceiver.class);
	    PendingIntent pending = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		if ( fActive ) {
			// �������� ��� �������� � �����
			saveBeacon();
			// ����� �������
			doBindService();
		    Calendar cal = Calendar.getInstance();
		    cal.add(Calendar.SECOND, 1);
		    
		    int nInterval = currentBeacon.interval * 60 * 1000;
		    service.setInexactRepeating(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),nInterval , pending);
	      	NetLog.Toast(NeverLostActivity.this, "������ �������...�������� %d ���",currentBeacon.interval);
			edit.putBoolean("active",true);
		} else {
			doUnbindService();
	      	service.cancel(pending);
	      	this.stopService(new Intent(this,TrackerService.class));
	      	Toast.makeText(NeverLostActivity.this, "������ ����������...",Toast.LENGTH_SHORT).show();
			edit.putBoolean("active",false);
		}
		edit.commit();
		return true;
	}
	
	void saveBeacon() {
		// ����� ����� �� �����...����� ���� �������� �� Enter - ��....
		currentBeacon.status = txtStatusText.getText().toString();

        // ����������....
		SharedPreferences prefs = NeverLostActivity.this.getSharedPreferences("prefs",1);
		SharedPreferences.Editor edit = prefs.edit();
		edit.putString("beaconID",currentBeacon.uid);
		edit.putString("beaconName",currentBeacon.name);
		edit.putString("login", currentBeacon.login);
		edit.putString("password", currentBeacon.password);
		edit.putInt("interval",currentBeacon.interval);
		edit.putInt("selectedBeaconIndex", currentBeacon.selectedBeaconIndex);
		edit.putString("statusText",currentBeacon.status);
		edit.commit();
	}
	
	boolean validateLogin(String sLogin,String sPassword) {
		
    	if ( sLogin.length() == 0 ) {
       	 Toast.makeText(NeverLostActivity.this, "������� �����!",Toast.LENGTH_SHORT).show();
       	 return false;
    	}
       	 else if ( sPassword.length() == 0 ) {
          	 Toast.makeText(NeverLostActivity.this, "������� ������!",Toast.LENGTH_SHORT).show();
          	 return false;
    	}
		return true;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		doUnbindService();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    return true;
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.miStatus:
              //  if ( isServiceRunning() ) {
                	Intent myIntent1 = new Intent(this, StatusActivity.class);
                	this.startActivityFromChild(this, myIntent1, 0);
               // } else 
                //	Toast.makeText(NeverLostActivity.this, "������� ��������� ������...",Toast.LENGTH_SHORT).show();
             return true;
             /*
	        case R.id.miSeatMate:
	        	Intent myIntent3 = new Intent(this, SeatmateActivity.class);
                startActivityForResult(myIntent3, 0);
	        	return true;
	        	*/
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	
} // NeverLostActivity Class

