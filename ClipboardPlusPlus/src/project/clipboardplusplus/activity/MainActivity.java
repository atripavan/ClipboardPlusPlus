package project.clipboardplusplus.activity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import project.clipboardplusplus.service.DetectCopyEventService;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ActionBar.Tab;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.nyu.clipboardplusplus.adapter.TabsPagerAdapter;

public class MainActivity extends FragmentActivity implements
ActionBar.TabListener {

	public static final String TAG = "MainActivity";
	public static final String APP_REG = "register_app";
	public static final String WEB_SRVC_URL = "http://clipboardplusaws-env-e33d5e3cep.elasticbeanstalk.com/clipboardplusplusws";
	public static String regId = "";
	public static Boolean isServiceStarted = false;
	public static final String SENDER_ID = "942270659912";
	private ViewPager viewPager;
	private TabsPagerAdapter mAdapter;
	private ActionBar actionBar;
	GoogleCloudMessaging gcm;
	// Tab titles
	private String[] tabs = { "Clips", "Timeline", "Frequent" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		SharedPreferences regSharedPref = getSharedPreferences(APP_REG, MODE_PRIVATE);
		regId = regSharedPref.getString("regid", "");
		if(regId.isEmpty()){
			registerWithGCM();
		}else{
			Log.i(TAG+":SharedPref", "Already exists regid");
		}
		if(!isServiceStarted)
			startService();

		viewPager = (ViewPager) findViewById(R.id.pager);
		actionBar = getActionBar();
		mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

		viewPager.setAdapter(mAdapter);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);      

		// Adding Tabs
		for (String tab_name : tabs) {
			actionBar.addTab(actionBar.newTab().setText(tab_name)
					.setTabListener(this));
		}

		/**
		 * on swiping the viewpager make respective tab selected
		 * */
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// on changing the page
				// make respected tab selected
				actionBar.setSelectedNavigationItem(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		
		showNotification();
	}


	// Start the service
	public void startService() {
		Log.d(TAG, "Starting activity");
		isServiceStarted = true;
		this.startService(new Intent(this, DetectCopyEventService.class));
	}


	public void registerWithGCM() {
		try {
			new AsyncTask<Void, Void, String>() {
				@Override
				protected String doInBackground(Void... params) {
					try {
						Log.d(TAG, "Registering with GCM....");
						if (gcm == null) {
							gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
						}
						regId = gcm.register(SENDER_ID);
						Log.i(TAG,  "GCM:"+regId);
						registerDeviceOnCloud(regId);
						Log.d(TAG, "Success Registering with GCM");

					} catch (IOException ex) {
						Log.e("GCM:Error",  ex.getMessage(),ex);
					}
					return regId;
				}
				@Override
				protected void onPostExecute(String msg) {
					SharedPreferences.Editor editor = getSharedPreferences(APP_REG, MODE_PRIVATE).edit();
					editor.putString("regid", regId);
					editor.commit();
					Log.d(TAG+":SharedPref", "Stored into Shared Pref");
				}

			}.execute(null, null, null);

		} catch (Exception e) {
			Log.e(TAG+":error while registering GCM", e.getMessage());
			e.printStackTrace();
		}
	}

	public String getUsernameFromAccounts(){
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
			Log.d(TAG+": getusername", email);
			return email;
		}else
			return null;
	}

	public String genDeviceId(){
		String androidId = Secure.getString(this.getContentResolver(),
				Secure.ANDROID_ID);
		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		String imei = telephonyManager.getDeviceId();

		return androidId + ":" + imei;
	}

	public boolean registerDeviceOnCloud(String regId){
		String username = getUsernameFromAccounts();
		String deviceId = genDeviceId();
		String description = "Used by "+username;
		String response = "";
		boolean result = false;
		try {
			final String temp = WEB_SRVC_URL+"?command=createdevice&deviceid=%s&regid=%s&username=%s&description=%s";
			String encodedUrl = String.format(temp, URLEncoder.encode(deviceId,"UTF-8"), URLEncoder.encode(regId,"UTF-8"),
					URLEncoder.encode(username,"UTF-8"),URLEncoder.encode(description, "UTF-8"));
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

			Log.i(TAG+"", "Response:"+response);

			if(response.contains("success")){
				result = true;
			} else
				result = false;	

		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return result;
	}

	@SuppressLint("NewApi")
	public void showNotification(){

		// define sound URI, the sound to be played when there's a notification
		Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		// intent triggered, you can add other intent for other actions
		Intent intent = new Intent(MainActivity.this, MainActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);

		// this is it, we'll build the notification!
		// in the addAction method, if you don't want any icon, just set the first param to 0
		Notification mNotification = new Notification.Builder(this)

		.setContentTitle("Clipboard++")
		.setContentText("Touch to see your clipboard history")
		.setSmallIcon(R.drawable.icon)
		.setContentIntent(pIntent)
		.setSound(soundUri)
		.setOngoing(true)
//		.addAction(R.drawable.icon, "View", pIntent)
//		.addAction(0, "Remind", pIntent)

		.build();

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		notificationManager.notify(0, mNotification);
	}

	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// on tab selected
		// show respected fragment view
		Log.d(TAG, "ontabselected");
		viewPager.setCurrentItem(tab.getPosition());
	}

	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
		// TODO Auto-generated method stub

	}
}
