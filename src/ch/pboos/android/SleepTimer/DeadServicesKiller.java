package ch.pboos.android.SleepTimer;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.content.Context;

public class DeadServicesKiller {

	private static List<String> deadServices = new ArrayList<String>();

	public static synchronized void addDeadService(String name){
		deadServices.add(name);
	}

	public static void killAddedServices(Context context) {
		ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		if(!deadServices.isEmpty()){
			for(String service: deadServices){
				am.restartPackage(service);
			}
			deadServices.clear();
		}
	}
}
