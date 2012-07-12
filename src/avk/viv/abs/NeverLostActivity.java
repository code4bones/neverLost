package avk.viv.abs;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
    public StateListener stateListener = null;
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
    
	private TrackerService trackerService;
	private ServiceConnection serviceConnection;
	boolean isServiceBound;
	
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
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
        
        // delete
        this.txtLogin.setText("bada");
        this.txtPassword.setText("bada");
		currentBeacon = new BeaconObj();
        
        // HTTP Gateway Utility Object
        gatewayUtil = new GatewayUtil(this,"mydevice");
        
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
				        spBeacons.setEnabled(true);
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
				
				currentBeacon.interval = NeverLostActivity.gatewayUtil.getFrequency(obj.uid);
				if ( currentBeacon.interval <= 0 ) {
					Toast.makeText(NeverLostActivity.this, gatewayUtil.responseMSG,Toast.LENGTH_SHORT).show();
					currentBeacon.interval = 10;
				}
				lbInterval.setText(String.format("Период обновления: %d мин.",currentBeacon.interval));
		        lbInterval.setTextColor(Color.WHITE);
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
				Log.v("clinch","Active ? " + fActive);
				txtLogin.setEnabled(!fActive);
				txtLogin.setClickable(!fActive);
				txtPassword.setEnabled(!fActive);
				txtPassword.setClickable(!fActive);
				bnFetchBeacons.setEnabled(!fActive);
				spBeacons.setEnabled(!fActive);
				controlService(fActive);
			}
		};
		
		// Запускаем сервис
		
		// Вешаем эвенты
		this.tgActive.setOnCheckedChangeListener(onChangeActive);
		this.bnFetchBeacons.setOnClickListener( onLoadBeacons );		
		this.spBeacons.setOnItemSelectedListener( onSelectBeacon );
		this.txtLogin.setOnKeyListener(onValidateInput);
		this.txtPassword.setOnKeyListener(onValidateInput);
	
		serviceConnection = new ServiceConnection() {
			public void onServiceConnected(ComponentName arg0, IBinder arg1) {
				 trackerService = ((TrackerService.TrackerBinder)arg1).getService();
				 Log.v("clinch","Service Connected");
				
			}
			public void onServiceDisconnected(ComponentName arg0) {
				 trackerService = null;
			}
		};
		
        // Monitors different states of device
        stateListener = new StateListener(this) {
        	@Override
        	public void handleMessage(Message msg) {
        		Log.v("clinch",(String)msg.obj);
        	}
        }; // StateListener
	} // onCreate

	void doBindService() {
		bindService(new Intent(this,TrackerService.class),serviceConnection,Context.BIND_AUTO_CREATE);
		isServiceBound = true;
	}
	
	void doUnbindService() {
		if ( isServiceBound ) {
			unbindService(serviceConnection);
			isServiceBound = false;
		}
	}
	
	boolean controlService(boolean fActive) {
		
		if ( fActive ) {
			this.startService(new Intent(this,TrackerService.class));
	      	Toast.makeText(NeverLostActivity.this, "Сервис стартовал !",Toast.LENGTH_SHORT).show();
		} else {
			this.stopService(new Intent(this,TrackerService.class));
	      	Toast.makeText(NeverLostActivity.this, "Сервис остановлен...",Toast.LENGTH_SHORT).show();
		}
		
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
	
} // NeverLostActivity Class


