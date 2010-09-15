package ch.pboos.android.SleepTimer.service;

import java.util.ArrayList;
import java.util.List;

import android.os.Binder;

public class SleepTimerServiceBinder extends Binder implements
		ISleepTimerService {

	private SleepTimerService _service;
	private List<ISleepTimerCallback> _callbacks = new ArrayList<ISleepTimerCallback>();
	
	public SleepTimerServiceBinder(SleepTimerService service) {
		_service = service;
	}
	
	@Override
	public void start(int minutes) {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void registerCallback(ISleepTimerCallback callback) {
		_callbacks.add(callback);
	}

	@Override
	public void unregisterCallback(ISleepTimerCallback callback) {
		_callbacks.remove(callback);
	}

}
