package ch.pboos.android.SleepTimer;

import ch.pboos.android.SleepTimer.service.SleepTimerService;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

public class SetVolumeBackReceiver extends BroadcastReceiver {

	public static final String INTENT_EXTRA_MUSIC_VOLUME = "musicVolume";
	
	@Override
	public void onReceive(Context context, Intent intent) {	
		SleepTimer.stopNotification(context);
		
		AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		if(audioManager.isMusicActive())
			SleepTimerService.sendStopBroadcast(context);

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if(!audioManager.isMusicActive()){
			int oldVolume = intent.getExtras().getInt(INTENT_EXTRA_MUSIC_VOLUME);
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, oldVolume, 0);
		}
		
		ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE); ; 
		am.restartPackage("ch.pboos.android.SleepTimer");
	}
}
