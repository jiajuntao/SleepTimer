package ch.pboos.android.SleepTimer.service;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class SleepTimerService extends Service {
	private SleepTimerServiceBinder _binder;
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("SleepTimerService", "Created");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.i("SleepTimerService", "Started");
		_binder = new SleepTimerServiceBinder(this);
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i("SleepTimerService", "Returning Binder");
		return _binder;
	}
}
