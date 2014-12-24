package project.clipboardplusplus.util;

import project.clipboardplusplus.service.ClipboardSynchronizeService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
 
public class NetworkChangeReceiver extends BroadcastReceiver {
    public static String TYPE_WIFI = "Clipboard++:connected to WiFi";
    public static String TYPE_MOBILE = "Clipboard++:Connected to Mobile Network";
    public static String TYPE_NOT_CONNECTED = "Clipboard++:Not connected to internet";
    public static String INTERNET_STATUS = "";
    public static final String TAG = "NetworkChangeReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
    	
        if(getConnectivityStatus(context)){

			Log.d(TAG, "Internet connected");
    		context.startService(new Intent(context, ClipboardSynchronizeService.class));
        }
        	
        Toast.makeText(context, INTERNET_STATUS, Toast.LENGTH_SHORT).show();
    }
    
    public static boolean getConnectivityStatus(Context context) {
    	boolean result = false;
    	INTERNET_STATUS = TYPE_NOT_CONNECTED;
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
 
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI){
            	INTERNET_STATUS = TYPE_WIFI;
                result = true;
            }
             
            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE){
            	INTERNET_STATUS = TYPE_MOBILE;
                result = true;
            }
        }
        return result;
    }     
}