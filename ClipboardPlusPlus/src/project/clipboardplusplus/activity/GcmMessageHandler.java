package project.clipboardplusplus.activity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import project.clipboardplusplus.database.ClipboardDatabaseHelper;

import com.google.android.gms.gcm.GoogleCloudMessaging;



//import project.clipboardplusplus.database.ClipboardDatabaseHelper;
import android.app.IntentService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class GcmMessageHandler extends IntentService {

	String mes;
	private Handler handler;
	public GcmMessageHandler() {
		super("GcmMessageHandler");
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		handler = new Handler();
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();

		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);
		Log.i("GCM", "Received Extras: "+extras.toString());

		mes = extras.getString("message");
		Log.d("GCM", "Received : (" +messageType+")  "+extras.getString("messsage"));
		GcmBroadcastReceiver.completeWakefulIntent(intent);
		
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		
		ClipboardDatabaseHelper dbHelper = new ClipboardDatabaseHelper(GcmMessageHandler.this);
		if(mes!=null && !mes.isEmpty()){
			dbHelper.insertClip(dateFormat.format(date), mes);
			ClipboardManager clipBoard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData myClip = ClipData.newPlainText("text", mes);
			clipBoard.setPrimaryClip(myClip);
			showToast();
		}
	}

	public void showToast(){
		handler.post(new Runnable() {
			public void run() {
				Toast.makeText(getApplicationContext(),"New Cloud Clip:"+mes , Toast.LENGTH_LONG).show();
			}
		});
	}
}