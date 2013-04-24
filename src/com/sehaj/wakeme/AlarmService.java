package com.sehaj.wakeme;

//Alarm service for waking up from background

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class AlarmService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();		
	}
	
	@Override
	public void onDestroy() {
		Model.deactivate();
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Alarm alarm = getNext();
		if(null != alarm){
			alarm.schedule(getApplicationContext());			
		}else{
			Intent myIntent = new Intent(getApplicationContext(), AlarmAlertBroadcastReciever.class);
			myIntent.putExtra("alarm", new Alarm());
			
			PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, myIntent,PendingIntent.FLAG_CANCEL_CURRENT);			
			AlarmManager alarmManager = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
			
			alarmManager.cancel(pendingIntent);
		}
		return START_NOT_STICKY;
	}

	private Alarm getNext(){
		Set<Alarm> alarmQueue = new TreeSet<Alarm>(new Comparator<Alarm>() {
			@Override
			public int compare(Alarm lhs, Alarm rhs) {
				int result = 0;
				long diff = lhs.getAlarmTime().getTimeInMillis() - rhs.getAlarmTime().getTimeInMillis();				
				if(diff>0){
					return 1;
				}else if (diff < 0){
					return -1;
				}
				return result;
			}
		});
				
		Model.init(getApplicationContext());
		List<Alarm> alarms = Model.getAll();
		
		for(Alarm alarm : alarms){
			if(alarm.getAlarmActive())
				alarmQueue.add(alarm);
		}
		if(alarmQueue.iterator().hasNext()){
			return alarmQueue.iterator().next();
		}else{
			return null;
		}
	}

}
