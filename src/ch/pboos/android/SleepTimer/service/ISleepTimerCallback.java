package ch.pboos.android.SleepTimer.service;

public interface ISleepTimerCallback {
	void stateUpdated(int currentState, int minutes);
}
