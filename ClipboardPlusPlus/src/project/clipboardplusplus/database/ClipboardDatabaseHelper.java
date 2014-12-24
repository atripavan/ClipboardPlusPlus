package project.clipboardplusplus.database;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;

public class ClipboardDatabaseHelper extends SQLiteOpenHelper{

	// Log
	private static final String TAG = "DatabaseHelperClass";

	// Database Version
	private static final int DATABASE_VERSION = 15;

	//Database Name
	private static final String DATABASE_NAME = "Clipdb.db";

	// Table Name
	public static final String TABLE_CLIP = "ClipTable";

	// TABLE_TRIP - column names
	public static final String KEY_CLIPDATE = "clip_date";
	public static final String KEY_CLIP = "clip";
	public static final String KEY_COUNT = "clip_count";

	public String colsClip[] ={KEY_CLIP,KEY_CLIPDATE,KEY_COUNT};


	public String[] getcolsClip() {
		return colsClip;
	}
	//Create Table statements
	//TABLE_TRIP
	private static final String CLIP_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "+ TABLE_CLIP 
			+ " (" + KEY_CLIPDATE + " DATETIME, " 
			+ KEY_CLIP + " TEXT, "
			+ KEY_COUNT + " INTEGER, "
			+ "PRIMARY KEY (" + KEY_CLIPDATE + ", " + KEY_CLIP + "))";


	public ClipboardDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CLIP_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLIP);
		onCreate(db);
	}

	public boolean insertClip(String clipDate, String clipContent) {
		boolean inserted = true;
//		Log.i(TAG, clipDate+clipContent);
		inserted = checkIfClipExists(clipContent);
		if(!inserted){
			SQLiteDatabase database = this.getWritableDatabase();
			ContentValues cv = new ContentValues();
			cv.put(KEY_CLIPDATE, clipDate);
			cv.put(KEY_CLIP, clipContent);
			cv.put(KEY_COUNT, 0);
			database.insert(TABLE_CLIP, null, cv);
			database.close();
		}
		return inserted;
	}

	public ArrayList<String> getAllClips()
	{		
		SQLiteDatabase database =getReadableDatabase();
		Cursor cursor = database.query(ClipboardDatabaseHelper.TABLE_CLIP, colsClip, null, null, null, null, null);
		ArrayList<String> clips = new ArrayList<String>();

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			if(cursor.getString(0)!=null && (!cursor.getString(0).isEmpty())){
//				Log.i(TAG, "Clip:"+cursor.getString(0)+
//						"Clip count:"+cursor.getString(2));
				clips.add(cursor.getString(0));
			} else{
				Log.i(TAG, "Cursor:Null string found");
			}
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		database.close();	
		return clips;
	}

	public boolean checkIfClipExists(String clipContent){
		if(clipContent.length()>40)
			clipContent = clipContent.substring(0, 40);
		SQLiteDatabase database =getReadableDatabase();
		Cursor cursor = database.rawQuery("SELECT "+KEY_COUNT+" FROM "+TABLE_CLIP+" where "+KEY_CLIP+" like '%"+clipContent+"%'", null);
				
		if(cursor.getCount()>0){		
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				if(cursor.getString(0)!=null && (!cursor.getString(0).isEmpty())){
					int count = Integer.parseInt(cursor.getString(0));
					count = count + 1;
					ContentValues values=new ContentValues();
					values.put(KEY_COUNT,count);
					database.update(TABLE_CLIP, values, KEY_CLIP+" like '%"+clipContent+"%'", null);
					Log.d(TAG, "CheckIfClipExists-TRUE, incremented count");
				} else{
					Log.i(TAG, "Clip count Cursor:Null string found");
				}
				cursor.moveToNext();
			}
			return true;
		}
		else{
			Log.d(TAG, "CheckIfClipExists-FALSE");
			return false;
		}		
	}

	public String getAllClipsFromDate(String dateTime){
		String delimiter = "|@|";
		String clips = "";
		SQLiteDatabase database = getReadableDatabase();
		//		database.rawQuery("DELETE FROM "+TABLE_CLIP+" where "+KEY_CLIP+" like '%"+clipContent+"%';", null);
		String query = "SELECT "+KEY_CLIP+" from "+TABLE_CLIP+" WHERE "+KEY_CLIPDATE+
				" > '"+dateTime+"'";
//		Log.d(TAG, query);
		Cursor cursor = database.rawQuery(query, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			if(cursor.getString(0)!=null && (!cursor.getString(0).isEmpty())){
				Log.i(TAG, "Date query cursor:"+cursor.getString(0));
				clips = clips + cursor.getString(0) + delimiter;
			} else{
				Log.i(TAG, "Date query cursor:Null string found");
			}
			cursor.moveToNext();
		}
		return clips;
	}

	public String deleteClip(String clipContent){
		if(clipContent.length()>40)
			clipContent = clipContent.substring(0, 40);
		SQLiteDatabase database =getReadableDatabase();
		//		database.rawQuery("DELETE FROM "+TABLE_CLIP+" where "+KEY_CLIP+" like '%"+clipContent+"%';", null);
		int rowsDel = database.delete(TABLE_CLIP, KEY_CLIP + " like '%"+clipContent+"%'", null);
		if(rowsDel > 0)
			Log.i(TAG, "Deleted clip from database:"+clipContent+"...");
		else
			Log.i(TAG, "No clip deleted");
		return clipContent;
	}

	public ArrayList<String> getDayClips(String yest)
	{
		String[] a = new String[1];
		a[0]       = yest + '%';
		
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_CLIP  + " WHERE " + KEY_CLIPDATE + " like ?", a);
		
		ArrayList<String> clipYestList = new ArrayList<String>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
				clipYestList.add(cursor.getString(cursor.getColumnIndex(KEY_CLIP)));
//				Log.d("Clips in DB: ", cursor.getString(cursor.getColumnIndex(KEY_CLIP)));
				cursor.moveToNext();
			
		}
		cursor.close();
		return clipYestList;
	}
	
	public ArrayList<String> getFrequentClips()
	{
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_CLIP  + " ORDER BY " + KEY_COUNT + " DESC limit 10;", null);

		ArrayList<String> clipYestList = new ArrayList<String>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
				clipYestList.add(cursor.getString(cursor.getColumnIndex(KEY_CLIP)));
				//Log.d("Frequent Clips in DB: ", cursor.getString(cursor.getColumnIndex(KEY_CLIP)));
				cursor.moveToNext();
			
		}
		cursor.close();
		return clipYestList;
	}
	
	public ArrayList<String> getAllSearched(String Clip_text)
	{
		String[] a = new String[1];
		a[0]       = '%' + Clip_text + '%';
		
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_CLIP  + " WHERE " + KEY_CLIP + " like ?", a);
		
		ArrayList<String> clipYestList = new ArrayList<String>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
				clipYestList.add(cursor.getString(cursor.getColumnIndex(KEY_CLIP)));
				//Log.d("Clips in DB: ", cursor.getString(cursor.getColumnIndex(KEY_CLIP)));
				cursor.moveToNext();
			
		}
		cursor.close();
		return clipYestList;
	}

}
