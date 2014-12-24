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
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.IntentService;
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

public class ClipboardSynchronizeService extends IntentService {
	public static final String TAG = "DetectCopyEventService";
	public static final String LAST_UPDATED_SP = "last_updated_sp";
	public static final String LAST_UPDATED_SP_KEY = "last_updated_dt";

	public ClipboardSynchronizeService() {
		super("ClipboardSynchronizeService");
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "Clipboard Sync Service triggered");
		ClipboardDatabaseHelper clipDbHelper = new ClipboardDatabaseHelper(getApplicationContext());
		String allClips = clipDbHelper.getAllClipsFromDate(getLastUpdatedSP());
		if(allClips!=null && !allClips.isEmpty()){
			Log.d(TAG, "Hitting web service to sync clips");
			new ClipboardAWSSyncService().execute(getUsername(), allClips, getDeviceId());
		}
	}
	
	public class ClipboardAWSSyncService extends AsyncTask<String, Void, Void> {

		@Override
		protected void onPreExecute() {
			Log.d(TAG, "In AsyncTask");
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(String... req) {
			String response = "";
			try {
				String username = req[0];
				String allClips = req[1];
				String deviceId = req[2];
				final String temp = MainActivity.WEB_SRVC_URL+"?command=insertallclips&deviceid=%s&username=%s&allclips=%s";
				String encodedUrl = String.format(temp, URLEncoder.encode(deviceId,"UTF-8"), URLEncoder.encode(username,"UTF-8"),
						URLEncoder.encode(allClips,"UTF-8"));
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

	
	public String getLastUpdatedSP(){
		SharedPreferences lastUpdSharedPref = getSharedPreferences(LAST_UPDATED_SP, MODE_PRIVATE);
		String lastUpdDateTime = lastUpdSharedPref.getString(LAST_UPDATED_SP_KEY, "");
		if(lastUpdDateTime.isEmpty()){
			SharedPreferences.Editor editor = getSharedPreferences(LAST_UPDATED_SP, MODE_PRIVATE).edit();
			lastUpdDateTime = ClipboardPlusPlusUtil.getCurDateTimeinString();
			editor.putString(LAST_UPDATED_SP_KEY, lastUpdDateTime);
			editor.commit();
		}
		return lastUpdDateTime;
	}

	public String getDeviceId() {
		String androidId = Secure.getString(this.getContentResolver(),
				Secure.ANDROID_ID);
		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		String imei = telephonyManager.getDeviceId();

		return androidId + ":" + imei;
	}

	public String getUsername(){
		AccountManager manager = AccountManager.get(this); 
		Account[] accounts = manager.getAccountsByType("com.google"); 
		List<String> possibleEmails = new LinkedList<String>();

		for (Account account : accounts) {
			// account.name as an email address only for certain account.type values.
			possibleEmails.add(account.name);
		}

		if(!possibleEmails.isEmpty() && possibleEmails.get(0) != null){
			String email = possibleEmails.get(0);
			Log.i(TAG+": getusername", email);
			return email;
		}else
			return null;
	}
}
