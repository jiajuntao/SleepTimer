package ch.pboos.android.SleepTimer.service;

import java.util.ArrayList;
import java.util.List;

import android.os.Binder;

public class SleepTimerServiceBinder extends Binder implements
		ISleepTimerService {

	private SleepTimerService _service;
	private List<ISleepTimerCallback> _callbacks = new ArrayList<ISleepTimerCallback>();
	private SleepTimerRunner _thread;
	
	public SleepTimerServiceBinder(SleepTimerService service) {
		_service = service;
	}
	
	@Override
	public void start(int minutes) {
		if(_thread==null) {
			_thread = new SleepTimerRunner(_service, minutes);
			_thread.start();
		}
	}

	@Override
	public void stop() {
		_thread.stopRunner();
		_thread = null;
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
