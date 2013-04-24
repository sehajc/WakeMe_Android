package com.sehaj.wakeme;

//Create, get, delete 1 or more alarms to and from database

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

import com.sehaj.wakeme.Alarm.Difficulty;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Model extends SQLiteOpenHelper {
	static Model instance = null;
	static SQLiteDatabase database = null;
	
	static final String DATABASE_NAME = "DB";
	static final int DATABASE_VERSION = 1;
	
	public static final String ALARM_TABLE = "alarm";
	public static final String COLUMN_ALARM_ID = "_id";
	public static final String COLUMN_ALARM_ACTIVE = "alarm_active";	
	public static final String COLUMN_ALARM_TIME = "alarm_time";
	public static final String COLUMN_ALARM_DAYS = "alarm_days";
	public static final String COLUMN_ALARM_DIFFICULTY = "alarm_difficulty";
	public static final String COLUMN_ALARM_TONE = "alarm_tone";
	public static final String COLUMN_ALARM_VIBRATE = "alarm_vibrate";
	public static final String COLUMN_ALARM_NAME = "alarm_name";
	public static final String COLUMN_ALARM_SMS_NO = "alarm_sms_no";
	public static final String COLUMN_ALARM_SMS_MESSAGE = "alarm_sms_message";
	
	public static void init(Context context) {
		if (null == instance) {
			instance = new Model(context);
		}
	}

	public static SQLiteDatabase getDatabase() {
		if (null == database) {
			database = instance.getWritableDatabase();
		}
		return database;
	}

	public static void deactivate() {
		if (null != database && database.isOpen()) {
			database.close();
		}
		database = null;
		instance = null;
	}

	public static long create(Alarm alarm) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(COLUMN_ALARM_ACTIVE, alarm.getAlarmActive());
		contentValues.put(COLUMN_ALARM_TIME, alarm.getAlarmTimeString());
		
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
		    ObjectOutputStream oos = null;
		    oos = new ObjectOutputStream(bos);
		    oos.writeObject(alarm.getDays());
		    byte[] buff = bos.toByteArray();
		    
		    contentValues.put(COLUMN_ALARM_DAYS, buff);
		    
		} catch (Exception e){
		}		
		
		contentValues.put(COLUMN_ALARM_DIFFICULTY, alarm.getDifficulty().ordinal());
		contentValues.put(COLUMN_ALARM_TONE, alarm.getAlarmTonePath());
		contentValues.put(COLUMN_ALARM_VIBRATE, alarm.getVibrate());
		contentValues.put(COLUMN_ALARM_NAME, alarm.getAlarmName());
		contentValues.put(COLUMN_ALARM_SMS_NO, alarm.getSmsNo());
		contentValues.put(COLUMN_ALARM_SMS_MESSAGE, alarm.getSmsMessage());
		
		return getDatabase().insert(ALARM_TABLE, null, contentValues);
	}
	public static int update(Alarm alarm) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(COLUMN_ALARM_ACTIVE, alarm.getAlarmActive());
		contentValues.put(COLUMN_ALARM_TIME, alarm.getAlarmTimeString());
		
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
		    ObjectOutputStream oos = null;
		    oos = new ObjectOutputStream(bos);
		    oos.writeObject(alarm.getDays());
		    byte[] buff = bos.toByteArray();
		    
		    contentValues.put(COLUMN_ALARM_DAYS, buff);
		    
		} catch (Exception e){
		}		
		
		contentValues.put(COLUMN_ALARM_DIFFICULTY, alarm.getDifficulty().ordinal());
		contentValues.put(COLUMN_ALARM_TONE, alarm.getAlarmTonePath());
		contentValues.put(COLUMN_ALARM_VIBRATE, alarm.getVibrate());
		contentValues.put(COLUMN_ALARM_NAME, alarm.getAlarmName());
		contentValues.put(COLUMN_ALARM_SMS_NO, alarm.getSmsNo());
		contentValues.put(COLUMN_ALARM_SMS_MESSAGE, alarm.getSmsMessage());
					
		return getDatabase().update(ALARM_TABLE, contentValues, "_id=" + alarm.getId(), null);
	}
	public static int deleteEntry(Alarm alarm){
		return deleteEntry(alarm.getId());
	}
	
	public static int deleteEntry(int id){
		return getDatabase().delete(ALARM_TABLE, COLUMN_ALARM_ID + "=" + id, null);
	}
	
	public static int deleteAll(){
		return getDatabase().delete(ALARM_TABLE, "1", null);
	}
	
	public static Alarm getAlarm(int id) {
		String[] columns = new String[] { 
				COLUMN_ALARM_ID, 
				COLUMN_ALARM_ACTIVE,
				COLUMN_ALARM_TIME,
				COLUMN_ALARM_DAYS,
				COLUMN_ALARM_DIFFICULTY,
				COLUMN_ALARM_TONE,
				COLUMN_ALARM_VIBRATE,
				COLUMN_ALARM_NAME,
				COLUMN_ALARM_SMS_NO,
				COLUMN_ALARM_SMS_MESSAGE
				};
		Cursor cursor = getDatabase().query(ALARM_TABLE, columns, COLUMN_ALARM_ID+"="+id, null, null, null,
				null);
		Alarm alarm = null;
		
		if(cursor.moveToFirst()){
			
			alarm =  new Alarm();
			alarm.setId(cursor.getInt(1));
			alarm.setAlarmActive(cursor.getInt(2)==1);
			alarm.setAlarmTime(cursor.getString(3));
			byte[] repeatDaysBytes = cursor.getBlob(4);
			
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(repeatDaysBytes);
			try {
				ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
				Alarm.Day[] repeatDays;
				Object object = objectInputStream.readObject();
				if(object instanceof Alarm.Day[]){
					repeatDays = (Alarm.Day[]) object;
					alarm.setDays(repeatDays);
				}								
			} catch (StreamCorruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
						
			alarm.setDifficulty(Difficulty.values()[cursor.getInt(5)]);
			alarm.setAlarmTonePath(cursor.getString(6));
			alarm.setVibrate(cursor.getInt(7)==1);
			alarm.setAlarmName(cursor.getString(8));
			alarm.setSmsNo(cursor.getString(9));
			alarm.setSmsMessage(cursor.getString(10));
			
		}
		cursor.close();
		return alarm;
	}
	
	public static Cursor getCursor() {
		String[] columns = new String[] { 
				COLUMN_ALARM_ID, 
				COLUMN_ALARM_ACTIVE,
				COLUMN_ALARM_TIME,
				COLUMN_ALARM_DAYS,
				COLUMN_ALARM_DIFFICULTY,
				COLUMN_ALARM_TONE,
				COLUMN_ALARM_VIBRATE,
				COLUMN_ALARM_NAME,
				COLUMN_ALARM_SMS_NO,
				COLUMN_ALARM_SMS_MESSAGE
				};
		return getDatabase().query(ALARM_TABLE, columns, null, null, null, null,
				null);
	}

	Model(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		db.execSQL("CREATE TABLE IF NOT EXISTS " + ALARM_TABLE + " ( " 
				+ COLUMN_ALARM_ID + " INTEGER primary key autoincrement, " 
				+ COLUMN_ALARM_ACTIVE + " INTEGER NOT NULL, " 
				+ COLUMN_ALARM_TIME + " TEXT NOT NULL, " 
				+ COLUMN_ALARM_DAYS + " BLOB NOT NULL, " 
				+ COLUMN_ALARM_DIFFICULTY + " INTEGER NOT NULL, "
				+ COLUMN_ALARM_TONE + " TEXT NOT NULL, " 
				+ COLUMN_ALARM_VIBRATE + " INTEGER NOT NULL, " 
				+ COLUMN_ALARM_NAME + " TEXT NOT NULL, "
				+ COLUMN_ALARM_SMS_NO + " TEXT NOT NULL, "
				+ COLUMN_ALARM_SMS_MESSAGE + " TEXT NOT NULL)"
				);
				
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + ALARM_TABLE);
		onCreate(db);
	}

	public static List<Alarm> getAll() {
		List<Alarm> alarms = new ArrayList<Alarm>();
		Cursor cursor = Model.getCursor();
		if (cursor.moveToFirst()) {

			do {
				Alarm alarm = new Alarm();
				alarm.setId(cursor.getInt(0));
				alarm.setAlarmActive(cursor.getInt(1) == 1);
				alarm.setAlarmTime(cursor.getString(2));
				byte[] repeatDaysBytes = cursor.getBlob(3);

				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
						repeatDaysBytes);
				try {
					ObjectInputStream objectInputStream = new ObjectInputStream(
							byteArrayInputStream);
					Alarm.Day[] repeatDays;
					Object object = objectInputStream.readObject();
					if (object instanceof Alarm.Day[]) {
						repeatDays = (Alarm.Day[]) object;
						alarm.setDays(repeatDays);
					}
				} catch (StreamCorruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

				alarm.setDifficulty(Difficulty.values()[cursor.getInt(4)]);
				alarm.setAlarmTonePath(cursor.getString(5));
				alarm.setVibrate(cursor.getInt(6) == 1);
				alarm.setAlarmName(cursor.getString(7));
				alarm.setSmsNo(cursor.getString(8));
				alarm.setSmsMessage(cursor.getString(9));
				
				alarms.add(alarm);

			} while (cursor.moveToNext());			
		}
		cursor.close();
		return alarms;
	}
}