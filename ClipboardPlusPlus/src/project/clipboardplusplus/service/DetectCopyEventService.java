package project.clipboardplusplus.service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import project.clipboardplusplus.activity.MainActivity;
import project.clipboardplusplus.database.ClipboardDatabaseHelper;
import project.clipboardplusplus.util.ClipboardPlusPlusUtil;
import project.clipboardplusplus.util.NetworkChangeReceiver;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class DetectCopyEventService extends Service implements
ClipboardManager.OnPrimaryClipChangedListener {
	public static final String TAG = "DetectCopyEventService";
	public static String username = "";
	public static final String LAST_UPDATED_SP = "last_updated_sp";
	public static final String LAST_UPDATED_SP_KEY = "last_updated_dt";

	ClipboardManager clipBoard;
	
	public DetectCopyEventService() {
	}
	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	@Override
	public void onCreate() {
		clipBoard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		clipBoard.addPrimaryClipChangedListener(this);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// Perform your long running operations here.
		Log.d(TAG, "Starting service Detect Copy Event Service");
//		Toast.makeText(this, "Detect Copy Event Service Started", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onDestroy() {
		Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
		clipBoard.removePrimaryClipChangedListener(this);
		MainActivity.isServiceStarted = false;
	}
	public void onPrimaryClipChanged() {
		ClipboardManager clipBoard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		String copiedString = "";
		ClipData.Item item = clipBoard.getPrimaryClip().getItemAt(0);
		copiedString = item.getText().toString();
		ClipboardDatabaseHelper obj = new ClipboardDatabaseHelper(getApplicationContext());
		if(!obj.insertClip(ClipboardPlusPlusUtil.getCurDateTimeinString(), copiedString)){
			
			Toast.makeText(getApplicationContext(), "Clipboard++ copied:\"" + copiedString + "\"",
					Toast.LENGTH_SHORT).show();
			//If INTERNET connectivity is on then send to cloud
			if(NetworkChangeReceiver.getConnectivityStatus(getApplicationContext())){
				new ClipboardAWSService().execute(getUsername(), copiedString.toString(), getDeviceId());
				updateLastUpdSP();				
			}
			
		} else{
			Toast.makeText(getApplicationContext(), "Clipboard++: Item already exists in history",
					Toast.LENGTH_SHORT).show();
		}
	}

	public String getDeviceId() {
		String androidId = Secure.getString(this.getContentResolver(),
				Secure.ANDROID_ID);
		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		String imei = telephonyManager.getDeviceId();

		return androidId + ":" + imei;
	}

	public void updateLastUpdSP(){
		SharedPreferences.Editor editor = getSharedPreferences(LAST_UPDATED_SP, MODE_PRIVATE).edit();
		String lastUpdDateTime = ClipboardPlusPlusUtil.getCurDateTimeinString();
		editor.putString(LAST_UPDATED_SP_KEY, lastUpdDateTime);
		editor.commit();
	}

	public String getUsername(){
		if(username.isEmpty()){
			AccountManager manager = AccountManager.get(this); 
			Account[] accounts = manager.getAccountsByType("com.google"); 
			List<String> possibleEmails = new LinkedList<String>();

			for (Account account : accounts) {
				// account.name as an email address only for certain account.type values.
				possibleEmails.add(account.name);
			}

			if(!possibleEmails.isEmpty() && possibleEmails.get(0) != null){
				String email = possibleEmails.get(0);
				/*String[] parts = email.split("@");
            if(parts.length > 0 && parts[0] != null)
                return parts[0];
            else
                return null;*/
				Log.i(TAG+": getusername", email);
				return email;
			}else
				return null;
		}else{
			return username;
		}
	}

	public class ClipboardAWSService extends AsyncTask<String, Void, Void> {

		@Override
		protected void onPreExecute() {
			Log.d(TAG, "In AsyncTask");
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(String... req) {
			String response = "";
			try {
				final String temp = MainActivity.WEB_SRVC_URL+"?command=insertclip&deviceid=%s&username=%s&clipcontent=%s";
				String encodedUrl = String.format(temp, URLEncoder.encode(req[2],"UTF-8"), URLEncoder.encode(req[0],"UTF-8"),
						URLEncoder.encode(req[1],"UTF-8"));
				Log.i(TAG, "Hitting URL:"+encodedUrl);
				URL url = new URL(encodedUrl);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");		

				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) 
				{ 
					response = null;
				}
				//Get Response
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				InputStream in = connection.getInputStream();

				int bytesRead = 0;
				byte[] buffer = new byte[1024];
				while((bytesRead = in.read(buffer)) > 0) {
					out.write(buffer,0,bytesRead);
				}
				out.close();
				in.close();
				response = new String(out.toByteArray());

				Log.i(TAG+": registerWithCloud response", response);


			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
			}
			return null;
		}
	}

	/*private void sendClipToCloud(String user, String copiedString, String deviceId) {
		String response = "";
		try {
			final String temp = MainActivity.WEB_SRVC_URL+"?command=insertclip&deviceid=%s&username=%s&clipcontent=%s";
			String encodedUrl = String.format(temp, URLEncoder.encode(deviceId,"UTF-8"), URLEncoder.encode(user,"UTF-8"),
					URLEncoder.encode(copiedString,"UTF-8"));
			URL url = new URL(encodedUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");		

			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) 
			{ 
				response = null;
			}
			//Get Response
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream in = connection.getInputStream();

			int bytesRead = 0;
			byte[] buffer = new byte[1024];
			while((bytesRead = in.read(buffer)) > 0) {
				out.write(buffer,0,bytesRead);
			}
			out.close();
			in.close();
			response = new String(out.toByteArray());

			Log.i(TAG+": registerWithCloud response", response);


		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}*/

}
