package project.clipboardplusplus.activity;

import java.util.ArrayList;

import project.clipboardplusplus.database.ClipboardDatabaseHelper;
import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class SupportSearch extends Activity {
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
 
        // get the action bar
        ActionBar actionBar = getActionBar();
 
        // Enabling Back navigation on Action Bar icon
        //actionBar.setDisplayHomeAsUpEnabled(true);
 
        //txtQuery = (TextView) findViewById(R.id.txtQuery);
 
        handleIntent(getIntent());
    }
 
	@Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }
	
	/**
     * Handling intent data
     */
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
 
            /**
             * Use this query to display search results like 
             * 1. Getting the data from SQLite and showing in listview 
             * 2. Making webrequest and displaying the data 
             * For now we just display the query only
             */
            //txtQuery.setText("Search Query: " + query);
            Log.d("in support", query);
            
            ClipboardDatabaseHelper dbHelper = new ClipboardDatabaseHelper(SupportSearch.this);
            ArrayList<String> allSearchClips = dbHelper.getAllSearched(query);
            for (String s : allSearchClips){
            	Log.d("String Retrieved", s); 
            }

            Intent i = new Intent(SupportSearch.this,MainActivity.class);
			i.putExtra("query", query);
			startActivity(i);
        }
 
    }
}
