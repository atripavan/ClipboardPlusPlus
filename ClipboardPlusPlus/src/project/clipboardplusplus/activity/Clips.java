package project.clipboardplusplus.activity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import project.clipboardplusplus.database.ClipboardDatabaseHelper;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.nyu.clipboardplusplus.adapter.ExpandableListAdapter;
import com.nyu.clipboardplusplus.adapter.TabsPagerAdapter;

import android.app.SearchManager;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


//public class Clips extends Fragment implements OnClickListener{
public class Clips extends Fragment {
	public static final String TAG = "ClipsFragment";
	public ClipboardDatabaseHelper clipDbHlpr;
	ListView lv_clips;
	Button bLoadClips;
	ArrayAdapter<String> allClipsAdptr;
	Context thiscontext;
	public static final int DELTA = 250 ;
	EditText inputSearch;
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
     	
    	thiscontext = container.getContext();
    	View rootView = inflater.inflate(R.layout.clips, container, false);
        
        clipDbHlpr = new ClipboardDatabaseHelper(thiscontext);
        
        lv_clips = (ListView) rootView.findViewById(R.id.clipsLv);
		setListenersOnClipsLV(lv_clips);
		loadClips(lv_clips);
		
	/**
      * Enabling Search Filter
      * */
		inputSearch = (EditText) rootView.findViewById(R.id.inputSearch);
		inputSearch.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
				// When user changed the Text
				Clips.this.allClipsAdptr.getFilter().filter(cs);	
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub							
			}
		});
        
        return rootView;
    }

	@Override
	public void onResume() {
		Log.i(TAG, "Frequent resume- loading clips...");
		loadClips(lv_clips);
		super.onResume();
	}

	public void setListenersOnClipsLV(ListView lv_clips) {
		lv_clips.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
	                int position, long id){
				String clipContent = (String)((TextView) v).getText();
				Log.i(TAG, "Placing clip onto clipboard:"+clipContent);
				placeOntoClipboard(clipContent);
			}
		});

		/*lv_clips.setOnTouchListener(new OnTouchListener() {
//			Float xCood = new Float(Float.NaN);
			float xCood = Float.NaN;
			String result = "";
			@Override
			public boolean onTouch(View v, MotionEvent event) {
//	        	Log.d(TAG, "Yout touched");
				result = "NONE";
		        switch (event.getAction()) 
		        {
		            case MotionEvent.ACTION_DOWN:
		            xCood = event.getX();
		            break;

		            case MotionEvent.ACTION_UP:
	            	Log.d(TAG, "Difference in coods:"+Float.toString((event.getX() - xCood)));
		            if (event.getX() - xCood < -DELTA) 
		            {
		            	Log.d(TAG, "Left Swipe");
		        		result = "LEFT";
		            }
		            else if (event.getX() - xCood > DELTA)  
		            {
		            	Log.d(TAG, "Right Swipe");
		            	result = "RIGHT";
		            } 
		            break;
		        }
				
				if(result.equalsIgnoreCase("RIGHT")){
					deleteListItem(event.getX(), event.getY());
					return true;
				}else{
					return false;
				}
			}
		});*/
		
		lv_clips.setOnItemLongClickListener(new OnItemLongClickListener() {
		    public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
		    	deleteListItem(position);
		        return true;
		    }
		});
	}
	

	public void deleteListItem(int itemPos){
		Log.d(TAG, "In deleteList Item");
		Object itemObj = lv_clips.getItemAtPosition(itemPos);
		if(itemObj!=null){
		String clipToDel = lv_clips.getItemAtPosition(itemPos).toString();
			Log.i(TAG, "Clip to del:"+clipToDel);
			allClipsAdptr.remove(lv_clips.getItemAtPosition(itemPos).toString());
			allClipsAdptr.notifyDataSetChanged();
			String delClip = clipDbHlpr.deleteClip(clipToDel);
			Toast.makeText(thiscontext, "Deleted:"+delClip, 
					Toast.LENGTH_SHORT).show();
		}		
	}
	
	/*public void deleteListItem(float xCood, float yCood){
		int itemPos = lv_clips.pointToPosition((int)xCood, (int)yCood);
		Object itemObj = lv_clips.getItemAtPosition(itemPos);
		if(itemObj!=null){
		String clipToDel = lv_clips.getItemAtPosition(itemPos).toString();
			Log.i(TAG, "Clip to del:"+clipToDel);
			allClipsAdptr.remove(lv_clips.getItemAtPosition(itemPos).toString());
			allClipsAdptr.notifyDataSetChanged();
			String delClip = clipDbHlpr.deleteClip(clipToDel);
			Toast.makeText(thiscontext, "Deleted:"+delClip, 
					Toast.LENGTH_SHORT).show();
		}		
	}*/
	
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
       inflater.inflate(R.menu.activity_main_action_bar, menu);
       // Associate searchable configuration with the SearchView
       SearchManager searchManager = (SearchManager) thiscontext.getSystemService(Context.SEARCH_SERVICE);
       SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
               .getActionView();
       searchView.setSearchableInfo(searchManager
               .getSearchableInfo(getActivity().getComponentName()));
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       // handle item selection
       switch (item.getItemId()) {
          case R.id.action_search:
        	  Log.d("in switch case: ", "action_search");
             return true;
          default:
             return super.onOptionsItemSelected(item);
       }
    }


	public void loadClips(ListView lv_clips){

		ArrayList<String> allClips = clipDbHlpr.getAllClips();
//		Log.d(TAG, "AL size:"+allClips.size());
//		for(String temp:allClips)
//			Log.d(TAG+":copiedstrings", "clipcontent:"+temp);
		allClipsAdptr = new ArrayAdapter<String>(
				thiscontext, android.R.layout.simple_list_item_1, allClips);
		lv_clips.setAdapter(allClipsAdptr);
	}
	
	public void placeOntoClipboard(String text){
		ClipboardManager clipBoard = (ClipboardManager) thiscontext.getSystemService(Service.CLIPBOARD_SERVICE);
		ClipData myClip = ClipData.newPlainText("text", text);
		clipBoard.setPrimaryClip(myClip);
		Toast.makeText(thiscontext, "Clipboard++: Text placed onto clipboard", 
					Toast.LENGTH_SHORT).show();
	}

	
}
