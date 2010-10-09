package ch.pboos.android.SleepTimer;

import ch.pboos.android.SleepTimer.service.SleepTimerService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

public class UnlockTools {

	public static boolean isAppPayed(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
	    return settings.getBoolean(context.getString(R.string.attr_ispayed), false);
	}
	
	protected static void setAppToPayed(Context context) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(context.getString(R.string.attr_ispayed), true);
		editor.commit();
		
		Intent intent = new Intent(context, SleepTimerService.class);
		intent.putExtra(SleepTimerService.EXTRA_ACTION, SleepTimerService.ACTION_UPDATE);
		context.startService(intent);
	}


	public static boolean isPackageAvailable(Context context, String packageName) {
        int sigMatch = context.getPackageManager().checkSignatures(context.getPackageName(), packageName); 
        return sigMatch == PackageManager.SIGNATURE_MATCH; 
	}
	

	public static boolean isPaidPackageInstalled(Context context) {
		if (isPackageAvailable(context, "ch.pboos.android.SleepTimerPayed")) {
			return true;
		}
		if (isPackageAvailable(context, "ch.pboos.android.SleepTimerPaid")) {
			return true;
		}
		
		return false;
	}
}
