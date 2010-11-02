package ch.pboos.android.SleepTimer.service;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.nullwire.trace.ExceptionHandler;

import ch.pboos.android.SleepTimer.R;
import ch.pboos.android.SleepTimer.SleepTimer;
import ch.pboos.android.SleepTimer.SleepTimerWidgetActivity;
import ch.pboos.android.SleepTimer.SleepTimerWidgetProvider;
import ch.pboos.android.SleepTimer.UnlockActivity;
import ch.pboos.android.SleepTimer.UnlockTools;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

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
	
	public static final String EXTRA_ACTION = "SLEEPTIMER_ACTION";
	public static final String EXTRA_MINUTES = "SLEEPTIMER_MINUTES";
	
	public static final int ACTION_STARTSTOP = 0;
	public static final int ACTION_UPDATE = 1;
	
	private int _currentState;
	
	@Override
	public void onCreate() {
		super.onCreate();
		ExceptionHandler.register(this, "http://pboos.ch/bugs/server.php");
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

	// This is the old onStart method that will be called on the pre-2.0
	// platform.  On 2.0 or later we override onStartCommand() so this
	// method will not be called.
	@Override
	public void onStart(Intent intent, int startId) {
	    handleCommand(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    handleCommand(intent);
	    // We want this service to continue running until it is explicitly
	    // stopped, so return sticky.
	    return START_STICKY;
	}

	private void handleCommand(Intent intent) {
		Log.i("SleepTimerService", "Started");
		if(intent!=null && intent.getExtras()!=null && intent.getExtras().containsKey(EXTRA_ACTION)) {
			int action = intent.getExtras().getInt(EXTRA_ACTION);
			switch (action) {
			case ACTION_STARTSTOP:
				if(isSleepTimerRunning()){
					stopSleepTimer();
				} else {
					startSleepTimer(intent.getExtras().getInt(EXTRA_MINUTES,5));
				}
				break;
			case ACTION_UPDATE:
				updateWidgets();
				break;
			}
		}
	}

	void updateWidgets() {
		ComponentName thisWidget = new ComponentName(this, SleepTimerWidgetProvider.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        RemoteViews remoteView;
        
        if(UnlockTools.isAppPayed(this)) {	
			remoteView = getUnlockedRemoteView();
		} else {
			remoteView = getLockedRemoteView();
		}
        
        manager.updateAppWidget(thisWidget, remoteView);
	}

	private RemoteViews getLockedRemoteView() {
		RemoteViews remoteView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.widget);
		int minutesInPreferences = getMinutesFromPreferences();
		if(isSleepTimerRunning()) {
			remoteView.setImageViewResource(R.id.Button_StartStop, android.R.drawable.ic_delete);
			remoteView.setTextViewText(R.id.text_minutes, String.format(getString(R.string.x_minutes), Integer.toString(_thread.getMinutesRemaining())));
		} else {
			remoteView.setImageViewResource(R.id.Button_StartStop, android.R.drawable.ic_media_play);
			remoteView.setTextViewText(R.id.text_minutes, String.format(getString(R.string.x_minutes), Integer.toString(minutesInPreferences)));
		}
		
		Intent intent = new Intent(this,UnlockActivity.class);
		
		remoteView.setOnClickPendingIntent(R.id.RelativeLayout01, PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
		return remoteView;
	}

	private RemoteViews getUnlockedRemoteView() {
		RemoteViews remoteView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.widget);
		int minutesInPreferences = getMinutesFromPreferences();
		Intent intent = new Intent(this,SleepTimerWidgetActivity.class);
		if(isSleepTimerRunning()) {
			remoteView.setImageViewResource(R.id.Button_StartStop, android.R.drawable.ic_delete);
			remoteView.setTextViewText(R.id.text_minutes, String.format(getString(R.string.x_minutes), Integer.toString(_thread.getMinutesRemaining())));
			intent.putExtra(SleepTimerWidgetActivity.EXTRA_IS_SLEEPTIMER_RUNNING, true);
		} else {
			remoteView.setImageViewResource(R.id.Button_StartStop, android.R.drawable.ic_media_play);
			remoteView.setTextViewText(R.id.text_minutes, String.format(getString(R.string.x_minutes), Integer.toString(minutesInPreferences)));
			intent.putExtra(SleepTimerWidgetActivity.EXTRA_IS_SLEEPTIMER_RUNNING, false);
		}
		remoteView.setOnClickPendingIntent(R.id.RelativeLayout01, PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
		return remoteView;
	}

	private int getMinutesFromPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(this).getInt(SleepTimer.PREFS_MINUTES, 5);
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
		updateWidgets();
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
