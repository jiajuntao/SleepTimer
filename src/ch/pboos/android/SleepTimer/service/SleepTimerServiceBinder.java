package ch.pboos.android.SleepTimer.service;

import android.os.Binder;

public class SleepTimerServiceBinder extends Binder implements
		ISleepTimerService {

	private SleepTimerService _service;
	
	
	public SleepTimerServiceBinder(SleepTimerService service) {
		_service = service;
	}
	
	@Override
	public void start(int minutes) {
		_service.startSleepTimer(minutes);
	}

	@Override
	public void stop() {
		_service.stopSleepTimer(false);
	}

	@Override
	public boolean isRunning() {
		return _service.isSleepTimerRunning();
	}

	@Override
	public void registerCallback(ISleepTimerCallback callback) {
		_service.registerCallback(callback);
	}

	@Override
	public void unregisterCallback(ISleepTimerCallback callback) {
		_service.unregisterCallback(callback);
	}

	@Override
	public void updateWidgets() {
		_service.updateWidgets();
	}
}
