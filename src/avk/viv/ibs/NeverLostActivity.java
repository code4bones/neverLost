package avk.viv.ibs;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Dialog;
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
import android.text.Html;
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
    
	final public static int kActionNone = 0;
	final public static int kActionActivate = 1;
	final public static int kActionDeactivate = 2;
	
	public int currentAction = kActionActivate;
	
	// GUI
	
	public Button	btnFetchBeacons;
	public Button   btnActivate;
	public TextView lbLogin;
	public EditText txtLogin;
	public EditText txtPassword;
	//public Spinner  spBeacons;
	//public TextView lbInterval;
	public EditText txtStatusText;
	public ToggleButton tbActivate;
	static public BeaconObj currentBeacon;
    
	public Intent statusIntent = null;
	static TrackerService trackerService = null;
	public ServiceConnection serviceConnection;
	public boolean isServiceBound;
	public boolean isRestoreState;

	////
	
	public static final PrintStream log = NetLog.Init("clinch","neverLost.log",true);
	
	public String getActionTitle() {
		String[] caption = {"*","Активировать","Деактивировать"};
		return caption[currentAction];
	}

	public void setLoginLabel() {
		this.lbLogin.setText(String.format("Логин / %s%s",
				currentBeacon.name != null && currentBeacon.name.length() > 0?currentBeacon.name:"",
				currentBeacon.authorized?" - Активирован":" - Не активирован"));
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        isServiceBound = false;
        
        // GUI Initialization
        this.lbLogin  = (TextView)findViewById(R.id.lbLogin);
        this.txtLogin = (EditText)findViewById(R.id.txtLogin);
        this.txtPassword = (EditText)findViewById(R.id.txtPassword);
        this.btnFetchBeacons = (Button)findViewById(R.id.btnFetchBeacons);
        this.txtStatusText = (EditText)findViewById(R.id.txtStatus);
        this.btnActivate = (Button)findViewById(R.id.btnActivate);
        this.tbActivate  = (ToggleButton)findViewById(R.id.tbActivate);
        
        this.btnActivate.setText(getActionTitle());
        
        // пока не залогинены - нахер
        
        currentBeacon = new BeaconObj();
        
       
		// Поднимаем старые настройки
		if ( currentBeacon.load(this) )
		{
			isRestoreState = true;
			NetLog.v("GUI: Saved settings are available...\n");
			NetLog.v("GUI: login = %s,password = %s,beaconID = %s,beaconName = %s interval = %d,auth = %s",
					currentBeacon.login,
					currentBeacon.password,
					currentBeacon.uid,
					currentBeacon.name,
					currentBeacon.interval,
					currentBeacon.authorized);
			currentAction = currentBeacon.authorized?kActionDeactivate:kActionActivate;
			setLoginLabel();
		}
		
		// TODO:: Remove 
		currentBeacon.login    = "bada"; //"v.g.0873@bk.ru";
		currentBeacon.password = "bada"; //"9d8x4";

		// Состояние контролсов
        boolean fRunning = isServiceRunning();
		if ( fRunning ) {
			NetLog.Toast(this,"Сервис уже запущен !");
			currentAction = kActionDeactivate;
		}
		
		this.txtLogin.setText(currentBeacon.login);
		this.txtPassword.setText(currentBeacon.password);
        this.txtStatusText.setText(currentBeacon.status);
        this.btnActivate.setText(getActionTitle());

        this.tbActivate.setEnabled(currentBeacon.authorized);
        this.tbActivate.setChecked(fRunning);
        this.btnActivate.setEnabled(!fRunning);
        this.btnFetchBeacons.setEnabled(!fRunning && !currentBeacon.authorized);
		this.txtLogin.setEnabled(!fRunning && !currentBeacon.authorized);
		this.txtPassword.setEnabled(!fRunning && !currentBeacon.authorized);
		this.txtStatusText.setEnabled(!fRunning && !currentBeacon.authorized);
        
        
        // HTTP Gateway Utility Object
        gatewayUtil = new GatewayUtil(this);
        
        // Setup Version information label
        PackageInfo pinfo;
		try {
			pinfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
	        String sTitle = this.getTitle().toString();
	        this.setTitle(String.format("%s / %s",sTitle,pinfo.versionName));
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} // try PackageInfo
		
		
		// Получение списка телепонов
		View.OnClickListener onSelectBeacon = new View.OnClickListener() {
			public void onClick(View v) {
				selectBeacon(txtLogin.getText().toString(),txtPassword.getText().toString());
			} // onClick
		}; // onSelectBeacon
		
		
		// Активация-Деактивация телефона
		View.OnClickListener onBeaconAction = new View.OnClickListener() {
			public void onClick(View v) {
				if ( activateBeacon(currentAction == kActionActivate) ) {
					currentAction = currentAction == kActionActivate?kActionDeactivate:kActionActivate;
					NeverLostActivity.this.btnActivate.setText(getActionTitle());
				} // activateBeacon(..
			} // onClick
		}; // onBeaconAction
		
		
		// Старт-Стоп сервиса
		tbActivate.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
				//	
				controlService(isChecked);
			}
			
		}); // Toggle service
		
		
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
                	else if ( txtEdit == txtPassword )
                   	 Toast.makeText(NeverLostActivity.this, "Введите пароль!",Toast.LENGTH_SHORT).show();
                	// prevent from resign
                	return fOk;
                } // if
                return fOk;
			} // onKey
		}; // OnKeyListener
		
		serviceConnection = new ServiceConnection() {
			public void onServiceConnected(ComponentName className, IBinder binder) {
				 trackerService = ((TrackerService.TrackerBinder)binder).getService();
			}
			public void onServiceDisconnected(ComponentName arg0) {
				 trackerService = null;
			}
		};
		
		// Вешаем эвенты
		this.btnFetchBeacons.setOnClickListener( onSelectBeacon );
		this.btnActivate.setOnClickListener( onBeaconAction );
		this.txtLogin.setOnKeyListener(onValidateInput);
		this.txtPassword.setOnKeyListener(onValidateInput);

		
		
	} // onCreate

	boolean selectBeacon(String sLogin,String sPassword) {

		if ( !validateLogin(sLogin,sPassword) )
			return false;

	    final Dialog dlg = new Dialog(this);
	    dlg.setContentView(R.layout.select_beacon);
	    final Button btnDone  = (Button)dlg.findViewById(R.id.btnDone);
	    final ListView listView = (ListView)dlg.findViewById(R.id.lbList);
		

		dlg.setTitle(Html.fromHtml("Телефон для активации"));
		
	    // установка дружбана для показа на карте
	    listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> item, View arg1, int position,long id) {
				BeaconObj obj = (BeaconObj)item.getAdapter().getItem(position);
				currentBeacon = obj;
				currentBeacon.login = txtLogin.getText().toString();
				currentBeacon.password = txtPassword.getText().toString();
				String sTitle = String.format("Выбран телефон - <b>%s<b>",currentBeacon.name);
				dlg.setTitle(Html.fromHtml(sTitle));
			}
	    });
	    
	    // Выбрали телефон
	    btnDone.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				dlg.dismiss();
				if ( currentBeacon == null ) 
					NetLog.Toast(NeverLostActivity.this,"Пользователь не выбран...");
				else {
					NetLog.Toast(NeverLostActivity.this,"Телефон \"%s\" выбран",currentBeacon.name);
					NeverLostActivity.this.btnActivate.setEnabled(true);
					setLoginLabel();
				}
			}
		});
	    
		FetchBeaconsTask task = new FetchBeaconsTask(gatewayUtil,sLogin,sPassword,null,false) {
			@Override
			public void OnComplete(ArrayList<BeaconObj> beaconList) {
				if ( beaconList == null ) {
					NetLog.MsgBox(NeverLostActivity.this,"Ошибка","Ошибка получения списка:%s",gatewayUtil.responseMSG);
			        dlg.dismiss();
					return;
				} // beacon not set
				if ( beaconList.size() == 0 ) {
					NetLog.MsgBox(NeverLostActivity.this,"Внимание","Нет зарегестрированных телефонов");
					dlg.dismiss();
					return;
				}
				BeaconObj[] beacons = beaconList.toArray(new BeaconObj[beaconList.size()]);
				BeaconArrayAdapter ad = new BeaconArrayAdapter(NeverLostActivity.this, android.R.layout.simple_list_item_checked, beacons,Color.WHITE);
				listView.setAdapter(ad);
				dlg.show();
			} // onComplete
		};
		task.execute((Void[])null);
		
		return true;
	}
	
	boolean activateBeacon(boolean fDoActivate) {
		
		final boolean fActivate = fDoActivate;
		// Авторизируемся в фоновом режиме
		final BackgroundTask<Boolean,BeaconObj> authTask = new BackgroundTask<Boolean,BeaconObj>(this) {
			
			public void onComplete(Boolean fAuthorized ) {
				if ( !fAuthorized )
					NetLog.MsgBox(NeverLostActivity.this, "Активация", "Не возможно активировать телефон: %s", gatewayUtil.responseMSG);
				else
					NetLog.Toast(this.context,"Телефон %s %s",currentBeacon.name,fActivate?"Авторизирован":"Деавторизирован");
				tbActivate.setEnabled(fActivate && fAuthorized);
				btnFetchBeacons.setEnabled(!fActivate);
				currentBeacon.authorized = fAuthorized && fActivate;
				setLoginLabel();
				saveBeacon();
			}
			
			@Override
			protected Boolean doInBackground(BeaconObj ... args) {
				
	        	if ( !gatewayUtil.Authorization(currentBeacon.login,currentBeacon.password, fActivate?currentBeacon.uid:null) ) 
			    	return false; 
			     else 
			 		currentBeacon.interval = NeverLostActivity.gatewayUtil.getFrequency(currentBeacon.uid);
					
	        	if ( currentBeacon.interval <= 0 ) {
						// 10 минуточек по умолчанию
						currentBeacon.interval = 10;
				}

				return true; 
			} // doInBgr
		}; // AuthTask
    	authTask.execute(currentBeacon);
	
		
		txtLogin.setEnabled(!fActivate);
		txtLogin.setClickable(!fActivate);
		txtPassword.setEnabled(!fActivate);
		txtPassword.setClickable(!fActivate);
		txtStatusText.setEnabled(!fActivate);
		
		
		return true;
	}
	
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
			NetLog.MsgBox(this, "Ошибка","Не удалось получить доступ к сервису");
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
			// загоняем наш бикончик в префы
			saveBeacon();
			// пошло поехало
			doBindService();
		    Calendar cal = Calendar.getInstance();
		    cal.add(Calendar.SECOND, 1);
		    
		    int nInterval = currentBeacon.interval * 60 * 1000;
		    service.setInexactRepeating(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),nInterval , pending);
	      	NetLog.Toast(NeverLostActivity.this, "Сервис запущен...интервал %d мин",currentBeacon.interval);
			edit.putBoolean("active",true);
		} else {
			doUnbindService();
	      	service.cancel(pending);
	      	this.stopService(new Intent(this,TrackerService.class));
	      	Toast.makeText(NeverLostActivity.this, "Сервис остановлен...",Toast.LENGTH_SHORT).show();
			edit.putBoolean("active",false);
		}
		btnActivate.setEnabled(!fActive);
		edit.commit();
		return true;
	}

	
	void saveBeacon() {
		currentBeacon.status = txtStatusText.getText().toString();
		currentBeacon.save(this);
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
	        case R.id.miStatus:
              //  if ( isServiceRunning() ) {
                	Intent myIntent1 = new Intent(this, StatusActivity.class);
                	this.startActivityFromChild(this, myIntent1, 0);
               // } else 
                //	Toast.makeText(NeverLostActivity.this, "Сначала запустите сервис...",Toast.LENGTH_SHORT).show();
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


