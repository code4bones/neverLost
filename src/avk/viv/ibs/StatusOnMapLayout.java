package avk.viv.ibs;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class StatusOnMapLayout extends Activity {

	Button btnSetStatusOnMap;
	EditText txtStatusOnMap;
	TextView labeSymbLeft;
	
	BeaconObj currentBeacon;
	
	public StatusOnMapLayout() {
		super();
		// TODO Auto-generated constructor stub
	}
	

	 public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.statusonmap);
	        
	        
	        labeSymbLeft = (TextView)findViewById(R.id.labeSymbLeft);
	        txtStatusOnMap = (EditText)findViewById(R.id.txtStatusOnMap);
	        btnSetStatusOnMap = (Button)findViewById(R.id.btnSetStatusOnMap);
	        
	        //NetLog.v("labeSymbLeft:%X  txtStatusOnMap:%X  btnSetStatusOnMap:%X", labeSymbLeft,txtStatusOnMap,btnSetStatusOnMap );
	        
	        NeverLostActivity ref = NeverLostActivity.GetInstance();
	       	              
	        currentBeacon = new BeaconObj();
	        currentBeacon.load(ref);
	       
	        NetLog.v("%s",currentBeacon.toString());
	        
	        //currentBeacon.save(NeverLostActivity.GetInstance());

	        txtStatusOnMap.setText(currentBeacon.status!=null?currentBeacon.status:"");
	        
	        btnSetStatusOnMap.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					String newStatus = txtStatusOnMap.getText().toString();
					if( newStatus != null && newStatus.length() > 0 )
					{
						currentBeacon.status = newStatus;
						currentBeacon.save(NeverLostActivity.GetInstance());
						NetLog.Toast(NeverLostActivity.GetInstance(),"Статус изменен");
					}
				}
			});

	 	}
	 

	 
		
	 
	   @Override
		protected void onDestroy() {
			super.onDestroy();
		}

	
}