package project.clipboardplusplus.activity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import project.clipboardplusplus.database.ClipboardDatabaseHelper;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import com.nyu.clipboardplusplus.adapter.ExpandableListAdapter;

public class ClipHistory extends Fragment {

	ExpandableListAdapter listAdapter;
	ExpandableListView expListView;
	List<String> listDataHeader;
	HashMap<String, List<String>> listDataChild;
	Context thiscontext;
	public ClipboardDatabaseHelper clipDbHlpr;
	public static final String TAG = "ClipHistory";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		//Get parent Activity context
		thiscontext = container.getContext();
		View rootView = inflater.inflate(R.layout.history, container, false);

		clipDbHlpr = new ClipboardDatabaseHelper(thiscontext);
		
		// get the listview
		expListView = (ExpandableListView) rootView.findViewById(R.id.lvExp);

		// preparing list data
		prepareListData();

		// Listview Group click listener
		expListView.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				prepareListData();
				Log.d(TAG, "On group click");
				return false;
			}
		});

		// Listview Group expanded listener
		expListView.setOnGroupExpandListener(new OnGroupExpandListener() {

			@Override
			public void onGroupExpand(int groupPosition) {

				Log.d(TAG, "On group expand");
				Toast.makeText(thiscontext,
						listDataHeader.get(groupPosition) + " Expanded",
						Toast.LENGTH_SHORT).show();
			}
		});

		// Listview Group collasped listener
		expListView.setOnGroupCollapseListener(new OnGroupCollapseListener() {

			@Override
			public void onGroupCollapse(int groupPosition) {
				Log.d(TAG, "On group collapse");
				Toast.makeText(thiscontext,
						listDataHeader.get(groupPosition) + " Collapsed",
						Toast.LENGTH_SHORT).show();

			}
		});

		// Listview on child click listener
		expListView.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				Log.d(TAG, "On child click");
				// TODO Auto-generated method stub
				Toast.makeText(thiscontext,
						listDataHeader.get(groupPosition)
						+ " : "
						+ listDataChild.get(
								listDataHeader.get(groupPosition)).get(
										childPosition), Toast.LENGTH_SHORT)
										.show();
				return false;
			}
		});
				
		return rootView;
	}

	private String getEarlierday(int off) {
		// TODO Auto-generated method stub
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		TimeZone tz1 = TimeZone.getTimeZone("EST");
		dateFormat.setTimeZone(tz1);
		Calendar cal = Calendar.getInstance();	
		
		cal.add(Calendar.DATE, off);
		
		Log.i("Yesterday's date: ", dateFormat.format(cal.getTime()));
		return dateFormat.format(cal.getTime());
	}

	/*
	 * Preparing the list data
	 */
	private void prepareListData() {
		listDataHeader = new ArrayList<String>();
		listDataChild = new HashMap<String, List<String>>();

		// Adding child data
		listDataHeader.add("Today");
		listDataHeader.add("Yesterday");
		listDataHeader.add("Last Week");
		
		List<String> today = new ArrayList<String>();
		String Tday = getEarlierday(0);
		today = clipDbHlpr.getDayClips(Tday);
				
		List<String> yesterday = new ArrayList<String>();
		String Yest = getEarlierday(-1);
		yesterday = clipDbHlpr.getDayClips(Yest);
		
		List<String> week = new ArrayList<String>();
		week.addAll(today);
		week.addAll(yesterday);
		for (int i=-2; i>=-6; i--){
			String L_Week = getEarlierday(i);
			week.addAll(clipDbHlpr.getDayClips(L_Week));
		}
		
		listDataChild.put(listDataHeader.get(0), today); // Header, Child data
		listDataChild.put(listDataHeader.get(1), yesterday);
		listDataChild.put(listDataHeader.get(2), week);		

		listAdapter = new ExpandableListAdapter(thiscontext, listDataHeader, listDataChild);
//		setting list adapter
		expListView.setAdapter(listAdapter);
	}




}
