package com.sehaj.wakeme;

//Alarm Alert Configuration when screen is locked

import android.content.Context;
import android.os.PowerManager;

public class StaticWakeLock {
	private static PowerManager.WakeLock wakeLock = null;

	@SuppressWarnings("deprecation")
	public static void lockOn(Context context) {
		PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		//Object flags;
		if (wakeLock == null)
			wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "MATH_ALARM");
		wakeLock.acquire();
	}

	//Release wake locks to conserve battery
	public static void lockOff(Context context) {
		try {
			if (wakeLock != null)
				wakeLock.release();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}