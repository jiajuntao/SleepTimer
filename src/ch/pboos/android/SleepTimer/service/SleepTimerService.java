package ch.pboos.android.SleepTimer.service;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
	@SuppressWarnings("unchecked")
	private static final Class[] mStartForegroundSignature = new Class[] { int.class, Notification.class};
	@SuppressWarnings("unchecked")
	private static final Class[] mStopForegroundSignature = new Class[] { boolean.class};
	private NotificationManager mNM;
	private Method mStartForeground;
	private Method mStopForeground;
	private Object[] mStartForegroundArgs = new Object[2];
	private Object[] mStopForegroundArgs = new Object[1];
	
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
		
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	    try {
	        mStartForeground = getClass().getMethod("startForeground", mStartForegroundSignature);
	        mStopForeground = getClass().getMethod("stopForeground", mStopForegroundSignature);
	    } catch (NoSuchMethodException e) {
	        // Running on an older platform.
	        mStartForeground = mStopForeground = null;
	    }
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
		stopForegroundCompat(SLEEP_TIMER_NOTIFICATION_ID);
	}

	public void startSleepTimer(int minutes) {
		if(_thread==null) {
			_thread = new SleepTimerRunner(this, minutes);
			_thread.start();
			
			Notification notification = createNotification(getString(R.string.notify_started), getString(R.string.notify_started));
			startForegroundCompat(SLEEP_TIMER_NOTIFICATION_ID, notification);
		}
	}

	public void stopSleepTimer() {
		if(_thread == null)
			return;
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
			stopForegroundCompat(SLEEP_TIMER_NOTIFICATION_ID);
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
	
	public void setNotification(String text, String additional){
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		
		Notification notification = createNotification(text, additional);
		
		// Pass to NotificationManager
		mNotificationManager.notify(SLEEP_TIMER_NOTIFICATION_ID, notification);
	}

	private Notification createNotification(String text, String additional) {
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
		return notification;
	}
	
	/**
	 * This is a wrapper around the new startForeground method, using the older
	 * APIs if it is not available.
	 */
	void startForegroundCompat(int id, Notification notification) {
	    // If we have the new startForeground API, then use it.
	    if (mStartForeground != null) {
	        mStartForegroundArgs[0] = Integer.valueOf(id);
	        mStartForegroundArgs[1] = notification;
	        try {
	            mStartForeground.invoke(this, mStartForegroundArgs);
	        } catch (InvocationTargetException e) {
	            // Should not happen.
	            Log.w("ApiDemos", "Unable to invoke startForeground", e);
	        } catch (IllegalAccessException e) {
	            // Should not happen.
	            Log.w("ApiDemos", "Unable to invoke startForeground", e);
	        }
	        return;
	    }

	    // Fall back on the old API.
	    setForeground(true);
	    mNM.notify(id, notification);
	}

	/**
	 * This is a wrapper around the new stopForeground method, using the older
	 * APIs if it is not available.
	 */
	void stopForegroundCompat(int id) {
	    // If we have the new stopForeground API, then use it.
	    if (mStopForeground != null) {
	        mStopForegroundArgs[0] = Boolean.TRUE;
	        try {
	            mStopForeground.invoke(this, mStopForegroundArgs);
	        } catch (InvocationTargetException e) {
	            // Should not happen.
	            Log.w("ApiDemos", "Unable to invoke stopForeground", e);
	        } catch (IllegalAccessException e) {
	            // Should not happen.
	            Log.w("ApiDemos", "Unable to invoke stopForeground", e);
	        }
	        return;
	    }

	    // Fall back on the old API.  Note to cancel BEFORE changing the
	    // foreground state, since we could be killed at that point.
	    mNM.cancel(id);
	    setForeground(false);
	}
}
