package avk.viv.abs;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.http.entity.*;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;

import android.content.Context;

import android.net.ConnectivityManager;
import android.os.Environment;
import android.util.Log;

public class GatewayUtil {
	
	// UID телефона
	public String deviceID;
	public Integer responseRC;
	public String  responseMSG;
	public Context context;
	
	public GatewayUtil(Context context_,String deviceID_) {
		context = context_;
		deviceID = deviceID_;
		Log.v("clinch","Gateway Initialized: UID = " + deviceID);
	}

	public boolean Authorization(String login,String pass,String beaconID) {
	    
		String sRequest;
		String sURL;
		
		if ( beaconID == null ) {
	        sRequest = ("//shluz.tygdenakarte.ru:60080/cgi-bin/Location_02?document=<request><function><name>PHONEFUNC_PKG.phone_authorization</name><index></index><param>%s^%s</param></function></request>");
	        sURL = String.format(sRequest,login,pass); 
	   }
	    else {
	        sRequest = "//shluz.tygdenakarte.ru:60080/cgi-bin/Location_02?document=<request><function><name>PHONEFUNC_PKG.phone_authorization</name><index></index><param>%s^%s^%s^-%s</param></function></request>";
	        sURL = String.format(sRequest,login,pass,beaconID,this.deviceID);
	    }
		
		if ( sendRequest(sURL) == false )
			return false;
		
		return responseRC >= 0;
	}
	
	ArrayList<BeaconObj> getBeaconList( String login,String password) {
	    ArrayList<BeaconObj> list = new ArrayList<BeaconObj>();
	
	    String sRequest = "//shluz.tygdenakarte.ru:60080/cgi-bin/Location_02?document=<request><function><name>PHONEFUNC_PKG_2.list_beacons</name><index>1</index><param>%s^%s</param></function></request>";
	    String sURL = String.format(sRequest, login,password);

	    if ( !sendRequest(sURL) )
	    	return list;
	    
	    StringTokenizer tokens = new StringTokenizer(responseMSG,"^");
	    
	    while ( tokens.hasMoreTokens() ) {
	    	list.add(BeaconObj.createWithString(tokens.nextToken()));
	    }
	    Log.v("clinch","beacons Count: " + list.size());

	    return list; 
	}
	
	public ArrayList<BeaconObj> getSeatMates(String beaconID) {

		ArrayList<BeaconObj> list = new ArrayList<BeaconObj>();
		   
		String sRequest = "http://shluz.tygdenakarte.ru:60080/cgi-bin/Location_02?document=<request><function><name>PHONEFUNC_PKG_2.get_seatmates</name><index>1</index><param>%s</param></function></request>";
		String sURL = String.format(sRequest, beaconID);

		if ( !sendRequest(sURL) )
			return null;
		
	    StringTokenizer tokens = new StringTokenizer(responseMSG,"^");
	    while ( tokens.hasMoreTokens() ) 
	    	list.add(BeaconObj.createWithString(tokens.nextToken()));
	    
	    Log.v("clinch","Seatmates Count: " + list.size());

	    return list; 
		
	}
	
	public BeaconObj getLastBeaconLocation(String beaconID) {
	    
		String sRequest = "//shluz.tygdenakarte.ru:60080/cgi-bin/Location_02?document=<request><function><name>PHONEFUNC_PKG_2.get_last_beacon_location</name><index>1</index><param>%s</param></function></request>";
		String sURL = String.format(sRequest,beaconID);
		
		if ( !sendRequest(sURL) ) 
			return null;
	
		if ( responseRC < 0 )
			return null;
		
		return BeaconObj.createWithLocationString(responseMSG);
	}
	
	public int getFrequency(String beaconID) {
		
		 String sRequest = "//shluz.tygdenakarte.ru:60080/cgi-bin/Location_02?document=<request><function><name>PHONEFUNC_PKG.get_frequency</name><index>1</index><param>%s</param></function></request>";    
		 String sURL = String.format(sRequest, beaconID);
		 
		 if ( !sendRequest(sURL) )
			 return 10;
		 
		return responseRC;
	}
	
	public boolean saveLocation(String beaconID, Double lng,Double lat,Double acc,String status) {
		
	    String sRequest = "//shluz.tygdenakarte.ru:60080/cgi-bin/Location_02?document=<request><function><name>PHONEFUNC_PKG.saveLocation_Phone_IPhnone</name><index>1</index><param>%s^%f^%f^%f^-%s^%s</param></function></request>";
	    String sURL = String.format(sRequest, beaconID,lng,lat,acc,this.deviceID,status);
    	String offlineFile = Environment.getExternalStorageDirectory() + "/LocationHistory.log";
	  
    	try {
		    if ( isConnected() ) {

	
		    	File fp = new File(offlineFile);
		    	fp.createNewFile();
		    	PrintWriter ps = new PrintWriter(fp);
		    	Format dtFmt = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		    	ps.printf("-1^%s^%f^%f^%f^-%s^%s^%s\n",beaconID,lng,lat,acc,this.deviceID,status,dtFmt.format(new Date()));
		    	ps.close();
		    	
		    	sendOfflineFile2(beaconID,offlineFile);
		    	
		    	//if ( !sendRequest(sURL)) 
		    		//return false;
		    } else {
		    	PrintStream ps = new PrintStream(offlineFile);
		    	Format dtFmt = new SimpleDateFormat("dd-MM-YYYY HH:mm:ss");
		    	ps.printf("-1^%s^%f^%f^%f^-%s^%s^%s\n",beaconID,lng,lat,acc,this.deviceID,status,dtFmt.format(new Date()));
		    	ps.close();
		    	Log.v("clinch","Dumped to " + offlineFile);
		    }  // not connected
	    } catch ( Exception e ) {
    		Log.v("clinch","saveLocaton:" + e.toString());
    	} // catch
		return true;
	}
	
	public boolean sendOfflineFile2(String beaconID,String offlineFile) {
	     HttpURLConnection connection = null ;
	     DataOutputStream outputStream = null ;

	     String lineEnd = "\r\n" ;
	     String twoHyphens = "--" ;
	     String boundary = "*****" ;

	     int bytesRead, bytesAvailable, bufferSize;
	     byte [] buffer;
	     int maxBufferSize = 1 * 1024 * 1024 ;

	     boolean sendSuccess = true ;

	     try {
	       FileInputStream fileInputStream = new FileInputStream ( new File (offlineFile )) ;

	       String sURL = ("http://shluz.tygdenakarte.ru:60080/cgi-bin/LocationFromFile_02");

	       URL url = new URL ( sURL ) ;
	       connection = ( HttpURLConnection ) url.openConnection () ;

	       // Allow Inputs & Outputs
	       connection.setDoInput ( true ) ;
	       connection.setDoOutput ( true ) ;
	       connection.setUseCaches ( false ) ;

	       // Enable POST method
	       connection.setRequestMethod ( "POST" ) ;

	       connection.setRequestProperty ( "Connection" , "Keep-Alive" ) ;
	       connection.setRequestProperty ( "Content-Type" , "multipart/form-data;boundary=" + boundary );
	       
	       outputStream = new DataOutputStream ( connection.getOutputStream ()) ;
	       outputStream.writeBytes ( twoHyphens + boundary + lineEnd ) ;
	       outputStream.flush () ;
	       
	       //---------------
	       outputStream.writeBytes ( "Content-Disposition: form-data; name=\"" + "device" + "\";" + lineEnd + lineEnd) ;
	       outputStream.flush () ;
	       outputStream.writeBytes ( beaconID ) ;
	       outputStream.flush () ;
	       outputStream.writeBytes ( twoHyphens + boundary + lineEnd ) ;
	       outputStream.flush () ;

	       //---------------		       

	       outputStream.writeBytes ( "Content-Disposition: form-data; name=\"" + "FormName" + "\";filename=\"" + offlineFile + "\"" + lineEnd ) ;
	       outputStream.flush () ;
	       outputStream.writeBytes ( lineEnd ) ;
	       outputStream.flush () ;
	       
	       bytesAvailable = fileInputStream.available () ;
	       bufferSize = Math.min ( bytesAvailable, maxBufferSize ) ;
	       buffer = new byte [ bufferSize ] ;

	       // Read file
	       bytesRead = fileInputStream.read ( buffer, 0 , bufferSize ) ;

	       while ( bytesRead > 0 ) 
	       {
	         outputStream.write ( buffer, 0 , bufferSize ) ;
	         outputStream.flush () ;
	         bytesAvailable = fileInputStream.available () ;
	         bufferSize = Math.min ( bytesAvailable, maxBufferSize ) ;
	         bytesRead = fileInputStream.read ( buffer, 0 , bufferSize ) ;
	       }

	       outputStream.writeBytes ( lineEnd ) ;
	       outputStream.flush () ;
	       outputStream.writeBytes ( twoHyphens + boundary + twoHyphens + lineEnd ) ;
	       outputStream.flush () ;
	       fileInputStream.close () ;
	       
	       outputStream.close () ;

	       connection.getResponseCode () ;
	       String msg = connection.getResponseMessage();
	       Log.v("clinch","response = " + msg);

	     } catch ( Exception e ) {
	    	 Log.v("clinch","Sending Request Error:" + e.toString());
	    	 sendSuccess = false ;
	     } finally {
	       connection.disconnect () ;
	     }
	     return sendSuccess;
	}
	
	public boolean sendOfflineFile(String offlineFile) {
		try {
			String b = "0xdeadfood";
			Log.v("clinch","Sending: " + offlineFile);
			String sURL = ("http://shluz.tygdenakarte.ru:60080/cgi-bin/LocationFromFile_02");
			File file = new File(offlineFile);
			
			HttpClient http = new DefaultHttpClient();
			HttpPost post = new HttpPost(sURL);
			
			//post.addHeader("Content-Type","multipart/form-data; boundary="+b);
			//post.addHeader("Content-Disposition","form-data;name=\"inputfile\"; filename=\""+offlineFile+"\"");
			
			//ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
			//nvp.add(new BasicNameValuePair("Content-Disposition","form-data;name=\"inputfile\"; filename=\""+offlineFile+"\""));
			//post.setEntity(new UrlEncodedFormEntity(nvp));
			
			StringBody body0 = new StringBody("Content-Disposition:form-data;name=\"inputfile\"; filename=\""+offlineFile+"\"");
			
			FileBody body = new FileBody(file);//,"multipart/form-data");

			MultipartEntity en = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			en.addPart("b1",body0);
			en.addPart("file",body);
			post.setEntity(en);
			
			
			HttpResponse response = http.execute(post);
			HttpEntity resEntity = response.getEntity();  
			String sXml = EntityUtils.toString(resEntity);
		    if (resEntity != null) {    
		      Log.v("clinch","XML = " + sXml);
		    }	
		    
		    parseResponse(sXml);
		    
		} catch ( Exception e ){
			Log.v("clinch","sendFile Error:" + e.toString());
			return false;
		}
		return true;
	}
	
	public void parseResponse(String xmlStr) {
		
		if ( xmlStr == null || xmlStr.length() == 0 )
			return;
		
		XMLParser xmlParser = new XMLParser();
		Document doc = xmlParser.getDomElement(xmlStr);
		
		responseMSG = new String(xmlParser.getValue(doc.getDocumentElement(), "msg"));
		String rc  = new String(xmlParser.getValue(doc.getDocumentElement(),"rc"));
		responseRC = new Integer(Integer.valueOf(rc)); 
	}
	
	public boolean sendRequest(String sURL) {
		
		try {
			URI sURI = new URI("http",sURL,null);
			sURL = sURI.toString();
			Log.v("clinch","Sending:" + sURL);
			
			XMLParser xmlParser = new XMLParser();
			String xmlStr = xmlParser.getXmlFromUrl(sURL);
			parseResponse(xmlStr);
			
			Log.v("clinch","RC = " + responseRC);
			Log.v("clinch","MSG = " + responseMSG);
			
		} catch ( Exception e ) {
			Log.v("clinch","Exception while sending request: " + e.toString());
			return false;
		}
		return true;
	}
	
	public boolean isConnected() {
	    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 		if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected())
 			return true;
 			return false;
	} 
}
