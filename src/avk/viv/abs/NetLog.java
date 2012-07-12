package avk.viv.abs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

public class NetLog {

	private static PrintStream ps = null; 
	private static String sLogFile = null;
	private static String TAG = null;
	
	public static PrintStream Init(String tag,String sLogFileName,boolean removeIfExists) {
		try {
			TAG = tag;
			sLogFile = Environment.getExternalStorageDirectory() + "/" + sLogFile;
			File file = new File(sLogFile);
			if ( file.exists() && removeIfExists ) {
				Log.v(TAG,"file removed");
				file.delete();
			}
			if ( ps == null )
				ps = new PrintStream(new FileOutputStream(file,true));
				return ps;
		} catch ( Exception e ) {
			Log.v(TAG,"NetLog Error " + e.toString());
		}
		return null;
	}
	
	public static void v(String fmt,Object ... args) {
		if ( ps != null )
			ps.printf(fmt,args);
		String s = String.format(fmt, args);
		Log.v(TAG,s);
	}
	
	public static void Dump() {
		File file = new File(sLogFile);
		try {
			String line = null;
			BufferedReader br = new BufferedReader( new InputStreamReader(new FileInputStream(file)));
			while ((line = br.readLine()) != null ) 
				Log.v(TAG,line);
			br.close();
		} catch ( Exception e ) {
			Log.v(TAG,"NetLog.Dump()=>"+e.toString());
		}
	}
};
