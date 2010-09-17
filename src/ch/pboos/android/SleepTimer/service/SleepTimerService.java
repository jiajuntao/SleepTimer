package ch.pboos.android.SleepTimer.service;


import java.util.ArrayList;
import java.util.List;

import ch.pboos.android.SleepTimer.R;
import ch.pboos.android.SleepTimer.SleepTimer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class SleepTimerService extends Service {
	private SleepTimerServiceBinder _binder;
	private List<ISleepTimerCallback> _callbacks = new ArrayList<ISleepTimerCallback>();
	private SleepTimerRunner _thread;
	private static final int SLEEP_TIMER_NOTIFICATION_ID = 47319;
	
	public static final int STATE_RUNNING = 0;
	public static final int STATE_STOPPED = 1;
	public static final int STATE_SHUTTING_DOWN = 2;
	private int _currentState;
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("SleepTimerService", "Created");
		_binder = new SleepTimerServiceBinder(this);
		_currentState = STATE_STOPPED;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.i("SleepTimerService", "Started");
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i("SleepTimerService", "Returning Binder");
		return _binder;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i("SleepTimerService", "Destroyed");
		_binder = null;
	}

	public void startSleepTimer(int minutes) {
		if(_thread==null) {
			_thread = new SleepTimerRunner(this, minutes);
			_thread.start();
		}
	}

	public void stopSleepTimer() {
		_thread.stopRunner();
		_thread = null;
		setState(SleepTimerService.STATE_STOPPED);
	}
	
	

	public boolean isSleepTimerRunning() {
		return _thread!=null;
	}

	public void unregisterCallback(ISleepTimerCallback callback) {
		_callbacks.remove(callback);
	}

	public void registerCallback(ISleepTimerCallback callback) {
		_callbacks.add(callback);
		
		int minutesRemaining = isSleepTimerRunning() ? _thread.getMinutesRemaining() : 0 ;
		callback.stateUpdated(_currentState, minutesRemaining);
	}

	public List<ISleepTimerCallback> getCallbacks() {
		return _callbacks;
	}
	
	void setMinutes() {
	}
	
	void setState(int state) {
		setState(state, 0);
	}
	
	void setState(int state, int minutes) {
		_currentState = state;
		
		switch (_currentState) {
		case STATE_RUNNING:
			showMinutesLeftNotification(minutes);
			break;
		
		case STATE_SHUTTING_DOWN:
			showGoingToSleepNotification();
			break;

		case STATE_STOPPED:
			removeNotification();
			break;
			
		default:
			break;
		}
		
		informAboutStateChange(minutes);
	}

	private void informAboutStateChange(int minutes) {
		for (ISleepTimerCallback callback : _callbacks) {
			callback.stateUpdated(_currentState, minutes);
		}
	}
	
	private void showGoingToSleepNotification() {
		setNotification(getResources().getString(R.string.notify_goingtosleep), getResources().getString(R.string.notify_goingtosleep2));
	}

	private void showMinutesLeftNotification(int minutes) {
		setNotification(minutes+" "+getResources().getString(R.string.notify_minutes_left), minutes+" "+getResources().getString(R.string.notify_minutes_left_until_sleep));
	}

	private void removeNotification() {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		mNotificationManager.cancel(SLEEP_TIMER_NOTIFICATION_ID);
	}
	
	public void setNotification(String text, String additional){
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		
		// Short Information
		int icon = R.drawable.sleep_icon;
		CharSequence tickerText = text;
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
		
		// Extended Information
		CharSequence contentTitle = "Sleep Timer";
		CharSequence contentText = additional;
		Intent notificationIntent = new Intent(this, SleepTimer.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(this, contentTitle, contentText, contentIntent);
		
		// Pass to NotificationManager
		mNotificationManager.notify(SLEEP_TIMER_NOTIFICATION_ID, notification);
	}
}
