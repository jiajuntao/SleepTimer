package ch.pboos.android.SleepTimer.service;

public interface ISleepTimerService {
	void start(int minutes);
	void stop();
	boolean isRunning();
	void registerCallback(ISleepTimerCallback callback);
	void unregisterCallback(ISleepTimerCallback callback);
}
