package ch.pboos.android.SleepTimer;

import java.util.ArrayList;
import java.util.List;

import ch.pboos.android.SleepTimer.service.SleepTimerService;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class KillDeadServicesReceiver  extends BroadcastReceiver {

	private static List<String> deadServices = new ArrayList<String>();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		killDeadServices(context);
		stopStopMusicService(context);
	}

	private void stopStopMusicService(Context context) {
		Intent serviceIntent = new Intent(context, SleepTimerService.class);
		context.stopService(serviceIntent);
	}

	private void killDeadServices(Context context) {
		ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		if(!deadServices.isEmpty()){
			for(String service: deadServices){
				am.restartPackage(service);
			}
			deadServices.clear();
		}
	}

	public static synchronized void addDeadService(String name){
		deadServices.add(name);
	}
}
