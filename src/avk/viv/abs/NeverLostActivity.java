package avk.viv.abs;

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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.*;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.location.*;
import android.net.*;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView.OnEditorActionListener;

public class NeverLostActivity extends Activity {
    /** Called when the activity is first created. */
    //public StateListener stateListener = null;
	public static GatewayUtil gatewayUtil = null;
    
	// GUI
	public Button	bnFetchBeacons;
	public TextView lbVersion;
	public EditText txtLogin;
	public EditText txtPassword;
	public Spinner  spBeacons;
	public TextView lbInterval;
	public ToggleButton tgActive;
	static public BeaconObj currentBeacon;
    
	static TrackerService trackerService = null;
	public ServiceConnection serviceConnection;
	public boolean isServiceBound;
	public boolean isRestoreState;
	public boolean isActive;
	
	public static final PrintStream log = NetLog.Init("clinch","neverLost.log",true);
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        NetLog.v("ACTIVITY STARTED\n");
 		
        isServiceBound = false;
        
        // GUI Initialization
        this.lbVersion = (TextView)findViewById(R.id.lbVersion);
        this.txtLogin = (EditText)findViewById(R.id.txtLogin);
        this.txtPassword = (EditText)findViewById(R.id.txtPassword);
        this.lbInterval = (TextView)findViewById(R.id.lbInterval);
        this.tgActive = (ToggleButton)findViewById(R.id.tgActive);
        this.spBeacons = (Spinner)findViewById(R.id.spBeacons);
        this.bnFetchBeacons = (Button)findViewById(R.id.sbFetchBeacons);
        
        // пока не залогинены - нахер
        this.tgActive.setEnabled(false);
        this.spBeacons.setEnabled(false);
        this.lbInterval.setTextColor(Color.DKGRAY);

        currentBeacon = new BeaconObj();
        isActive = false;
        
		SharedPreferences prefs = this.getSharedPreferences("prefs", 1);
		// Поднимаем старые настройки
		if ( prefs.getString("login", null) != null )
		{
			isRestoreState = true;
			NetLog.v("GUI: Saved settings are available...\n");
			currentBeacon.login    = prefs.getString("login", "");
			currentBeacon.password = prefs.getString("password", "");
			currentBeacon.uid      = prefs.getString("beaconID", "");
			currentBeacon.name     = prefs.getString("beaconName", "");
			currentBeacon.interval = prefs.getInt("interval", 10);
			currentBeacon.selectedBeaconIndex = prefs.getInt("selectedBeaconIndex", 0);
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
		
		// Получение списка телепонов и инициализация спиннера
		View.OnClickListener onLoadBeacons = new View.OnClickListener() {
			public void onClick(View v) {
				
				String sLogin = txtLogin.getText().toString();
				String sPassword = txtLogin.getText().toString();
				
				if ( !validateLogin(sLogin,sPassword) )
					return;
				
				// Фоновый режим....
				FetchBeaconsTask task = new FetchBeaconsTask(NeverLostActivity.gatewayUtil,sLogin,sPassword,null,false) {
					@Override
					public void OnComplete(ArrayList<BeaconObj> beaconList) {
						if ( beaconList == null ) {
							Toast.makeText(NeverLostActivity.this, gatewayUtil.responseMSG,Toast.LENGTH_SHORT).show();
					        tgActive.setEnabled(false);
					        spBeacons.setEnabled(false);
					        lbInterval.setTextColor(Color.DKGRAY);
					        return;
						}
						BeaconObj[] beacons = beaconList.toArray(new BeaconObj[beaconList.size()]);
						BeaconArrayAdapter ad = new BeaconArrayAdapter(NeverLostActivity.this, android.R.layout.simple_spinner_item, beacons);
						ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						spBeacons.setAdapter(ad);	
						spBeacons.setPrompt("Выберете...");
						tgActive.setEnabled(true);
				        spBeacons.setEnabled(!isActive);
						// Если востанавлеваем значения из sharedprefs...
						if ( isRestoreState )
							spBeacons.setSelection(currentBeacon.selectedBeaconIndex);
					}
				};
				task.execute((Void[])null);
			}
		}; // OnClickListener

		// Выбираем активный телефон из спиннера
		OnItemSelectedListener onSelectBeacon = new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> item,View view,int position,long id) {
				BeaconObj obj = (BeaconObj)item.getAdapter().getItem(position);
				currentBeacon = obj;
				currentBeacon.login = txtLogin.getText().toString();
				currentBeacon.password = txtPassword.getText().toString();
				currentBeacon.interval = NeverLostActivity.gatewayUtil.getFrequency(obj.uid);
				currentBeacon.selectedBeaconIndex = position;
				if ( currentBeacon.interval <= 0 ) {
					Toast.makeText(NeverLostActivity.this, gatewayUtil.responseMSG,Toast.LENGTH_SHORT).show();
					currentBeacon.interval = 10;
				}
				
				lbInterval.setText(String.format("Период обновления: %d мин.",currentBeacon.interval));
		        lbInterval.setTextColor(Color.WHITE);
		        
		        // Сохранимся....
				SharedPreferences prefs = NeverLostActivity.this.getSharedPreferences("prefs",1);
				SharedPreferences.Editor edit = prefs.edit();
				edit.putString("beaconID",currentBeacon.uid);
				edit.putString("beaconName",currentBeacon.name);
				edit.putString("login", currentBeacon.login);
				edit.putString("password", currentBeacon.password);
				edit.putInt("interval",currentBeacon.interval);
				edit.putInt("selectedBeaconIndex", currentBeacon.selectedBeaconIndex);
				edit.commit();
			
			}
			public void onNothingSelected(AdapterView<?> arg) {
			}
		};
		
		
		// Запрещаем пустые поля логина и пароля
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
                	 Toast.makeText(NeverLostActivity.this, "Введите логин!",Toast.LENGTH_SHORT).show();
                	else 
                   	 Toast.makeText(NeverLostActivity.this, "Введите пароль!",Toast.LENGTH_SHORT).show();
                	// prevent from resign
                } // if
                return fOk;
			} // onKey
		}; // OnKeyListener
		
		
		// Остановка/Запуск сервиса
		CompoundButton.OnCheckedChangeListener onChangeActive = new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton arg0, boolean fActive) {
				NetLog.v("clinch","Set service state to %d\n",fActive);
				txtLogin.setEnabled(!fActive);
				txtLogin.setClickable(!fActive);
				txtPassword.setEnabled(!fActive);
				txtPassword.setClickable(!fActive);
				bnFetchBeacons.setEnabled(!fActive);
				spBeacons.setEnabled(!fActive);
				controlService(fActive);
			}
		};
		
		serviceConnection = new ServiceConnection() {
			public void onServiceConnected(ComponentName className, IBinder binder) {
				 trackerService = ((TrackerService.TrackerBinder)binder).getService();
				 NetLog.v("Service Connected\n");
			}
			public void onServiceDisconnected(ComponentName arg0) {
				 trackerService = null;
				 NetLog.v("Service Disconnected...\n");
			}
		};
		

		// Вешаем эвенты
		this.tgActive.setOnCheckedChangeListener(onChangeActive);
		this.bnFetchBeacons.setOnClickListener( onLoadBeacons );		
		this.spBeacons.setOnItemSelectedListener( onSelectBeacon );
		this.txtLogin.setOnKeyListener(onValidateInput);
		this.txtPassword.setOnKeyListener(onValidateInput);

		// Состояние контролсов
		if ( (this.isActive = isServiceRunning()) == true ) {
			NetLog.v("Service is already Running...");
			tgActive.setChecked(true);
			tgActive.setEnabled(true);
		}
		
		// если есть предыдущее состояние - восстанавливаем значение в спиннере
		if ( isRestoreState ) 
			bnFetchBeacons.performClick();
	} // onCreate

	boolean isServiceRunning() {
		String srvName = TrackerService.class.getName();
		ActivityManager mgr = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
		for ( RunningServiceInfo srv : mgr.getRunningServices(Integer.MAX_VALUE)) {
			NetLog.v("Service: %s",srv.service.getClassName());
			if ( srvName.equals(srv.service.getClassName()))
				return true;
		}
		return false;
	}
	
	void doBindService() {
		if ( bindService(new Intent(this,TrackerService.class),serviceConnection,Context.BIND_AUTO_CREATE) ) {
			NetLog.v("Service successfuly binded...\n");
			isServiceBound = true;
		} else {
			NetLog.v("Failed to bind service\n");
		}
	}
	
	void doUnbindService() {
		if ( isServiceBound ) {
			NetLog.v("Unbinding service...\n");
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
			doBindService();
		    Calendar cal = Calendar.getInstance();
		    cal.add(Calendar.SECOND, 1);
		    
		    service.setInexactRepeating(AlarmManager.RTC_WAKEUP,
						cal.getTimeInMillis(), OnBootReceiver.REPEAT_TIME, pending);
	      	
		    // Авторизация активного телефона
		    if ( !gatewayUtil.Authorization(currentBeacon.login,currentBeacon.password, currentBeacon.uid) )
				Toast.makeText(NeverLostActivity.this, gatewayUtil.responseMSG ,Toast.LENGTH_SHORT).show();
	      	else {
	      		Toast.makeText(NeverLostActivity.this, "Сервис запущен...",Toast.LENGTH_SHORT).show();
	      		NetLog.v("Activation: %d",gatewayUtil.responseRC);
	      	}
			edit.putBoolean("active",true);
		} else {
			doUnbindService();
	      	service.cancel(pending);
	      	this.stopService(new Intent(this,TrackerService.class));
	      	Toast.makeText(NeverLostActivity.this, "Сервис остановлен...",Toast.LENGTH_SHORT).show();
			edit.putBoolean("active",false);
		}
		edit.commit();
		return true;
	}
	
	boolean validateLogin(String sLogin,String sPassword) {
		
    	if ( sLogin.length() == 0 ) {
       	 Toast.makeText(NeverLostActivity.this, "Введите логин!",Toast.LENGTH_SHORT).show();
       	 return false;
    	}
       	 else if ( sPassword.length() == 0 ) {
          	 Toast.makeText(NeverLostActivity.this, "Введите пароль!",Toast.LENGTH_SHORT).show();
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
	        case R.id.miStart:
	        	Intent myIntent = new Intent(this, NeverLostActivity.class);
                startActivityForResult(myIntent, 0);
	            return true;
	        case R.id.miStatus:
	        	Intent myIntent2 = new Intent(this, StatusActivity.class);
                startActivityForResult(myIntent2, 0);
	            return true;
	      /*
	        case R.id.miSeatMate:
	        	//Intent myIntent3 = new Intent(this, StatusActivity.class);
                //startActivityForResult(myIntent3, 0);
	            return true;
	            */
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}	
} // NeverLostActivity Class


