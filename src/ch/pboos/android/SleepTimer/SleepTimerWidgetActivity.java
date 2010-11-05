package ch.pboos.android.SleepTimer;

import ch.pboos.android.SleepTimer.service.SleepTimerService;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class SleepTimerWidgetActivity extends Activity {

	private static final int REQUEST_MINUTES = 0;
	public static final String EXTRA_IS_SLEEPTIMER_RUNNING = "extra_is_sleeptimer_running";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		boolean isRunning = getIntent().getBooleanExtra(EXTRA_IS_SLEEPTIMER_RUNNING, false);
		
		if(isRunning) {
			stopSleepTimer();
			finish();
		} else  if (shouldSetMinutes()){
			Intent i = new Intent(this, SetTimeDialog.class);
			i.putExtra("minutes", getCurrentMinutes());
			startActivityForResult(i, REQUEST_MINUTES);
		} else {
			if (shouldStartMusicPlayer()) {
				startMusicPlayer();
			}
			startSleepTimer(getCurrentMinutes());
			finish();
		}
	}

	private void stopSleepTimer() {
		Intent intent = new Intent(this, SleepTimerService.class);
		intent.putExtra(SleepTimerService.EXTRA_ACTION, SleepTimerService.ACTION_STOP_SERVICE);
		startService(intent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(data!=null && data.hasExtra("minutes")){
			if (shouldStartMusicPlayer()) {
				startMusicPlayer();
			}
	    	int sleep_minutes = data.getIntExtra("minutes", getCurrentMinutes());
        	startSleepTimer(sleep_minutes);
        	finish();
    	}
	}

	private void startSleepTimer(int minutes) {
		Intent intent = new Intent(this, SleepTimerService.class);
		intent.putExtra(SleepTimerService.EXTRA_ACTION, SleepTimerService.ACTION_STARTSTOP);
		intent.putExtra(SleepTimerService.EXTRA_MINUTES, minutes);
		startService(intent);
	}

	private void startMusicPlayer() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String musicPlayerPackage = settings.getString(SleepTimer.PREFS_MUSIC_PLAYER, "");
		Intent musicPlayerIntent = getPackageManager().getLaunchIntentForPackage(musicPlayerPackage);
    	if(musicPlayerIntent==null){
			Toast.makeText(
					this,
					this.getResources().getString(
							R.string.pref_music_player_not_set),
					Toast.LENGTH_LONG).show();
    	} else {
			try {
				startActivity(musicPlayerIntent);
			} catch(ActivityNotFoundException e){
				String newMusicPlayerPackage = musicPlayerPackage;
				newMusicPlayerPackage = newMusicPlayerPackage.replace("/com.android.internal.app", "");
				if(newMusicPlayerPackage.equals("com.android.music")){
					musicPlayerIntent = new Intent("com.android.music.PLAYBACK_VIEWER");
					startActivity(musicPlayerIntent);
				} else {
					Toast.makeText(
							this,
							getResources().getString(
									R.string.error_cannot_start_app),
							Toast.LENGTH_LONG).show();
				}
			}
    	}
	}

	private boolean shouldStartMusicPlayer() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        return settings.getBoolean(getString(R.string.attr_widget_startplayer), true);
	}

	private int getCurrentMinutes() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        return settings.getInt(SleepTimer.PREFS_MINUTES, 5);
	}

	private boolean shouldSetMinutes() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        return settings.getBoolean(getString(R.string.attr_widget_setminutes), false);
	}
}
