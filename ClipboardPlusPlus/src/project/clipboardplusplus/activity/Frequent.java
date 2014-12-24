package project.clipboardplusplus.activity;

import java.util.ArrayList;




import project.clipboardplusplus.database.ClipboardDatabaseHelper;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Frequent extends Fragment {
 
	public static final String TAG = "ClipsFragment";
	public ClipboardDatabaseHelper clipDbHlpr;
	ListView lv_clips;
	ArrayAdapter<String> allClipsAdptr;
	Context thiscontext;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
    	thiscontext = container.getContext();
    	View rootView = inflater.inflate(R.layout.clips, container, false);
        EditText srchEt = (EditText)rootView.findViewById(R.id.inputSearch);
        srchEt.setVisibility(View.INVISIBLE);
        clipDbHlpr = new ClipboardDatabaseHelper(thiscontext);
        lv_clips = (ListView) rootView.findViewById(R.id.clipsLv);
         
        loadFreqClips();
        
        return rootView;
    }
    
    public void loadFreqClips(){

		ArrayList<String> allClips = clipDbHlpr.getFrequentClips();
//		Log.d(TAG, "AL size:"+allClips.size());
//		for(String temp:allClips)
//			Log.d(TAG+":copiedstrings", "clipcontent:"+temp);
		//ListView lv_clips = (ListView) findViewById(R.id.clipsLv);
		allClipsAdptr = new ArrayAdapter<String>(
				thiscontext, android.R.layout.simple_list_item_1, allClips);
		lv_clips.setAdapter(allClipsAdptr);
		lv_clips.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
	                int position, long id){
				String clipContent = (String)((TextView) v).getText();
				Log.i(TAG, "Placing clip onto clipboard:"+clipContent);
				placeOntoClipboard(clipContent);
			}
		});
	}
    
    public void placeOntoClipboard(String text){
		ClipboardManager clipBoard = (ClipboardManager) thiscontext.getSystemService(Service.CLIPBOARD_SERVICE);
		ClipData myClip = ClipData.newPlainText("text", text);
		clipBoard.setPrimaryClip(myClip);
		Toast.makeText(thiscontext, "Clipboard++: Text placed onto clipboard", 
					Toast.LENGTH_SHORT).show();
	}
 }
