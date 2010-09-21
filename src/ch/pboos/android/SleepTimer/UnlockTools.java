package ch.pboos.android.SleepTimer;

import android.content.Context;
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
	}


	public static boolean isPackageAvailable(Context context, String packageName) {
        int sigMatch = context.getPackageManager().checkSignatures(context.getPackageName(), packageName); 
        return sigMatch == PackageManager.SIGNATURE_MATCH; 
	}
}
