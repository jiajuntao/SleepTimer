package ch.pboos.android.SleepTimer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SleepTimerStatus {
	private static final String PREF_SLEEP_TIMER_IS_RUNNING = "SleepTimerIsRunning";

	public static boolean isRunning(Context context){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		return pref.getBoolean(PREF_SLEEP_TIMER_IS_RUNNING, false);
	}
	
	public static void setRunning(Context context, boolean isRunning){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean(PREF_SLEEP_TIMER_IS_RUNNING, isRunning);
		editor.commit();
	}
}
