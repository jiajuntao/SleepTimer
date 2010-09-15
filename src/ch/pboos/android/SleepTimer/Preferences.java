package ch.pboos.android.SleepTimer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

public class Preferences extends PreferenceActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		Preference musicAppPref = (Preference) findPreference("pref_musicapp");
		musicAppPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				final Preference pref = arg0;
//				Toast.makeText(getApplicationContext(), getResources().getString(R.string.dialog_loadapps_standby), Toast.LENGTH_LONG).show();
				
				Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
				mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
				PackageManager appInfo = getPackageManager();
				List<ResolveInfo> list = appInfo.queryIntentActivities(mainIntent, 0);
				Collections.sort(list, new ResolveInfo.DisplayNameComparator(appInfo));

				List<String> appsName = new ArrayList<String>();
				List<String> appsPackage = new ArrayList<String>();
				for(ResolveInfo app: list){
					appsName.add(app.activityInfo.applicationInfo.loadLabel(appInfo).toString());
					appsPackage.add(app.activityInfo.applicationInfo.packageName);
				}
				
				final String[] test = {};
				final String[] appsArray = appsName.toArray(test);
				final String[] appsPackageArray = appsPackage.toArray(test);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(Preferences.this);
				builder.setTitle(getResources().getString(R.string.pref_music_player));
				builder.setItems(appsArray, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				    	setOwnPreference(pref, appsArray[item], appsPackageArray[item], true);				    	Preference prefMusicAppName = new Preference(Preferences.this);
				    	prefMusicAppName.setKey(SleepTimer.PREFS_MUSIC_PLAYER_NAME);
				    	setOwnPreference(prefMusicAppName, "", appsArray[item], false);
				    }
				});

				AlertDialog alert = builder.create();
				alert.show();
				return true;
			}
		});
		
		///////////////////////////////////////////////////
		///////////////////////////////////////////////////
		///////////////////////////////////////////////////
		
		Preference ownServicePref = (Preference) findPreference("ownService");
		ownServicePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				final Preference pref = arg0;
//				Toast.makeText(getApplicationContext(), getResources().getString(R.string.dialog_loadapps_standby), Toast.LENGTH_LONG).show();
				
				Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
				mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
				PackageManager appInfo = getPackageManager();
				List<ResolveInfo> list = appInfo.queryIntentActivities(mainIntent, 0);
				Collections.sort(list, new ResolveInfo.DisplayNameComparator(appInfo));

				List<String> appsName = new ArrayList<String>();
				List<String> appsPackage = new ArrayList<String>();
				appsName.add(getResources().getString(R.string.own_service_empty));
				appsPackage.add("");
				for(ResolveInfo app: list){
					appsName.add(app.activityInfo.applicationInfo.loadLabel(appInfo).toString());
					appsPackage.add(app.activityInfo.applicationInfo.packageName);
				}
				
				final String[] test = {};
				final String[] appsArray = appsName.toArray(test);
				final String[] appsPackageArray = appsPackage.toArray(test);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(Preferences.this);
				builder.setTitle(getResources().getString(R.string.pref_app_to_stop));
				builder.setItems(appsArray, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				    	setOwnPreference(pref, appsArray[item], appsPackageArray[item], true);
				    }
				});

				AlertDialog alert = builder.create();
				alert.show();
				return true;
			}
		});
	}

	private void setOwnPreference(final Preference pref, final String chosen_simple, final String chosen, boolean showToast) {
		if(showToast){
			String text = String.format(getResources().getString(R.string.notify_pref_chosen), chosen_simple);
			Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
		}
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(Preferences.this);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(pref.getKey(), chosen);
		editor.commit();
	}
}
