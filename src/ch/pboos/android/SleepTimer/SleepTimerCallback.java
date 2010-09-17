package ch.pboos.android.SleepTimer;

import android.os.Handler;
import ch.pboos.android.SleepTimer.service.ISleepTimerCallback;
import ch.pboos.android.SleepTimer.service.SleepTimerServiceBinder;

public class SleepTimerCallback implements ISleepTimerCallback {

	//private SleepTimerServiceBinder service;
	private SleepTimer activity;
	private Handler handler;
	
	public SleepTimerCallback(SleepTimer activity, Handler handler, SleepTimerServiceBinder service) {
		//this.service = service;
		this.activity = activity;
		this.handler = handler;
	}
	
	@Override
	public void stateUpdated(final int state, final int minutes) {
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				activity.updateSleepTimerState(state, minutes);
			}
		});
	}

}
