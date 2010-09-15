package ch.pboos.android.SleepTimer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ch.pboos.android.SleepTimer.Bluetooth.BluetoothService;
import ch.pboos.android.SleepTimer.StopConnections.AndroidMediaPlayerStopperServiceConnection;
import ch.pboos.android.SleepTimer.StopConnections.HtcMediaPlayerStopperServiceConnection;

import com.nullwire.trace.ExceptionHandler;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.KeyEvent;

public class StopMusicService extends Service {
	int oldMusicVolumeLevel = 0;

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		ExceptionHandler.register(this, "http://pboos.ch/bugs/server.php");

		oldMusicVolumeLevel = dimMusicVolume();
		AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		if(audioManager.isMusicActive()){
			sendStopBroadcast(this);
			sleep(300);
			sendPauseBroadcast(this);
		}
		sleep(300);
		stopMusic();
		additionalStopSettings();
		killDeadServices();
		setVolumeBack(oldMusicVolumeLevel);
		SleepTimerStatus.setRunning(this, false);
	}

	private void sleep(long time) {
		// give the apps some time to save their state before killing.
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void additionalStopSettings() {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean turnOffWifi = settings.getBoolean("pref_stop_wifi", false);
		boolean turnOffBluetooth = settings.getBoolean("pref_stop_bluetooth",
				false);
		boolean goIntoAirplane = settings.getBoolean("pref_go_airplane", false);
		boolean turnOffNotifications = settings.getBoolean(
				"pref_mute_notifications", false);

		if (turnOffWifi)
			turnOffWifi();
		if (turnOffBluetooth)
			turnOffBluetooth();
		if (turnOffNotifications)
			muteNotifications();
		if (goIntoAirplane)
			goIntoAirplaneMode();
	}

	private void turnOffBluetooth() {
		BluetoothService service = BluetoothService.getInstance();
		service.setApplication(this.getApplication());
		service.stopBluetooth();
	}

	private void goIntoAirplaneMode() {
		boolean isEnabled = Settings.System.getInt(this.getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, 0) == 1;
		if (!isEnabled) {
			// toggle airplane mode
			Settings.System.putInt(this.getContentResolver(),
					Settings.System.AIRPLANE_MODE_ON, isEnabled ? 0 : 1);

			// Post an intent to reload
			Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
			intent.putExtra("state", !isEnabled);
			sendBroadcast(intent);
		}
	}

	private void muteNotifications() {
		AudioManager manager = (AudioManager) getSystemService(AUDIO_SERVICE);
		manager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0);
	}

	private void turnOffWifi() {
		WifiManager mWm = (WifiManager) this
				.getSystemService(Context.WIFI_SERVICE);
		if (mWm.isWifiEnabled())
			mWm.setWifiEnabled(false);
	}

	private int dimMusicVolume() {
		AudioManager manager = (AudioManager) getSystemService(AUDIO_SERVICE);
		int vol = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
		for (int i = vol - 1; i >= 0; i--) {
			manager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
			try {
				Thread.sleep(700);
			} catch (InterruptedException e) {
			}
		}
		return vol;
	}

	private void stopMusic() {
		List<String> restartMusicServices = getFixSetServices();

		ActivityManager am = (ActivityManager) this
				.getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(50);
		for (int i = 0; i < rs.size(); i++) {
			ActivityManager.RunningServiceInfo rsi = rs.get(i);

			String serviceName = rsi.service.getClassName();
			String processName = rsi.process;

			if (isHtcPlayer(serviceName))
				stopHtcPlayer();
			if (isAndroidPlayer(serviceName))
				stopAndroidPlayer();

			if (restartMusicServices.contains(processName)) {
				int index;
				if ((index = processName.indexOf(":")) > 0)
					processName = processName.substring(0, index);

				forceStopPackage(am, processName);
			}
		}

		List<String> restartMusicApps = getFixSetApps();
		restartMusicApps.add(getOwnService());
		List<ActivityManager.RunningAppProcessInfo> apps = am
				.getRunningAppProcesses();
		for (int i = 0; i < apps.size(); ++i) {
			ActivityManager.RunningAppProcessInfo app = apps.get(i);
			String processName = app.processName;

			if (restartMusicApps.contains(processName))
				forceStopPackage(am, processName);
		}
	}

	public static void sendPauseBroadcast(Context context) {
		long eventtime = SystemClock.uptimeMillis();

		Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
		KeyEvent downEvent = new KeyEvent(eventtime, eventtime,
				KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0);
		downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
		context.sendOrderedBroadcast(downIntent, null);

		Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
		KeyEvent upEvent = new KeyEvent(eventtime, eventtime+1,
				KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0);
		upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
		context.sendOrderedBroadcast(upIntent, null);
	}
	
	public static void sendStopBroadcast(Context context) {
		long eventtime = SystemClock.uptimeMillis();

		Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
		KeyEvent downEvent = new KeyEvent(eventtime, eventtime,
				KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_STOP, 0);
		downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
		context.sendOrderedBroadcast(downIntent, null);

		Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
		KeyEvent upEvent = new KeyEvent(eventtime, eventtime+1,
				KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_STOP, 0);
		upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
		context.sendOrderedBroadcast(upIntent, null);
	}

	private void forceStopPackage(ActivityManager am, String pkgName) {

		int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
		if (sdkVersion == 8 && RootUtils.hasRoot(this, false)) {
			try {
				Process process = Runtime.getRuntime().exec("su");
				DataOutputStream os = new DataOutputStream(process
						.getOutputStream());
				BufferedReader osRes = new BufferedReader(
						new InputStreamReader(process.getInputStream()));

				os.writeBytes("ps | grep " + pkgName + "\n");
				os.flush();

				String result = osRes.readLine();
				if(result!=null){
					int spaceIndex = result.indexOf(" ");
					while (result.charAt(spaceIndex++) == ' ') {
					}
					int spaceAfter = result.indexOf(" ", spaceIndex);
					String processId = result.substring(spaceIndex - 1,
							spaceAfter);
					os.writeBytes("kill " + processId + "\n");
					os.flush();
	
					os.writeBytes("exit\n");
					os.flush();
				}
				process.waitFor();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			am.restartPackage(pkgName);
		}
	}

	private boolean isAndroidPlayer(String serviceName) {
		return serviceName.equals("com.android.music.MediaPlaybackService");
	}

	private boolean isHtcPlayer(String serviceName) {
		return serviceName.equals("com.htc.music.MediaPlaybackService");
	}

	private void stopAndroidPlayer() {
		Intent intent = new Intent();
		intent.setClassName("com.android.music",
				"com.android.music.MediaPlaybackService");
		AndroidMediaPlayerStopperServiceConnection conn = new AndroidMediaPlayerStopperServiceConnection();
		bindService(intent, conn, 0);
		unbindService(conn);
	}

	private void stopHtcPlayer() {
		Intent intent = new Intent();
		intent.setClassName("com.htc.music",
				"com.htc.music.MediaPlaybackService");
		HtcMediaPlayerStopperServiceConnection conn = new HtcMediaPlayerStopperServiceConnection();
		bindService(intent, conn, 0);
		unbindService(conn);
	}

	private List<String> getFixSetApps() {
		// TODO: Listen in Array in Resources speichern?
		// getResources().getStringArray(R.array....)
		List<String> restartMusicApps = new ArrayList<String>();

		restartMusicApps.add("com.astroplayerbeta"); // AstroPlayer - works
		restartMusicApps.add("com.astroplayer");
		restartMusicApps.add("com.grooveshark.android.v1"); // Grooveshark
		restartMusicApps.add("radiotime.player"); // Radiotime
		restartMusicApps.add("com.spodtronic.radio.nrkradio"); // NRK Radio
		restartMusicApps.add("com.bmayers.bTunes"); // bTunes (beta)
		restartMusicApps.add("com.bmayers.bTunesRelease"); // bTunes
		restartMusicApps.add("org.abrantix.rockon.rockonnggl"); // 3, Cubed
		restartMusicApps.add("com.spotify.mobile.android.ui"); // Spotify
		restartMusicApps.add("com.magellandiscovery.radiotime"); // MDRadio
		restartMusicApps.add("de.stohelit.folderplayer"); // MortPlayer
		restartMusicApps.add("com.pandora"); // Pandora
		restartMusicApps.add("com.pandora.android"); // Pandora
		restartMusicApps.add("com.sec.android.app.music"); // Experia Music Player
		restartMusicApps.add("daapps.media.npr.paid"); // NPR Paid
		restartMusicApps.add("com.mathias.android.acast"); // ACast
		restartMusicApps.add("com.mathias.android.acast2"); // ACast
		restartMusicApps.add("com.mathias.android.acast3"); // ACast
		restartMusicApps.add("com.mathias.android.acast3free"); // ACast
		restartMusicApps.add("com.clearchannel.iheartradio.controller");// iheartradio
		restartMusicApps.add("fm.last.android"); // Last.fm (see as well below)
		restartMusicApps.add("com.otiasj.androradio"); // AndroRadio
		restartMusicApps.add("com.htc.fm"); // HTC FM Radio
		restartMusicApps.add("com.google.android.apps.listen"); // Google Listen
		restartMusicApps.add("com.stitcher.app"); // Stitcher
		restartMusicApps.add("com.samsung.sec.android.MusicPlayer"); // Samsung Music Player
		restartMusicApps.add("com.yyqidian.musiconline"); // Musiconline
		restartMusicApps.add("com.mecanto"); // Mecanto
		restartMusicApps.add("com.skysoft.kkbox.android"); //Kkbox
		restartMusicApps.add("org.freecoder.android.cmplayer.v6"); // RockPlayer
		restartMusicApps.add("org.freecoder.android.cmplayer.v7"); // RockPlayer
		restartMusicApps.add("com.mixzing.basic"); // MixZing
		restartMusicApps.add("com.r2"); // r2player
		restartMusicApps.add("com.bamnetworks.mobile.android.gameday"); // mlb at bat 2010
		
		return restartMusicApps;
	}

	private List<String> getFixSetServices() {
		// TODO: Listen in Array in Resources speichern?
		// getResources().getStringArray(R.array....)
		List<String> restartMusicServices = new ArrayList<String>();

		// not in anymore because now again through pause in service
		// restartMusicServices.add("com.android.music"); // works
		// restartMusicServices.add("com.htc.music"); // works

		restartMusicServices.add("com.streamfurious.android.free"); // works
		restartMusicServices.add("com.google.android.apps.listen:remote"); // works
		restartMusicServices.add("com.google.android.apps.listen"); // (not
																	// needed i
																	// think,
																	// but just
																	// for
																	// making
																	// sure
		restartMusicServices.add("com.imeem.gynoid"); // works
		restartMusicServices.add("fm.last.android:remote"); // works
		restartMusicServices.add("fm.last.android:player"); // works
		restartMusicServices.add("org.abrantes.filex"); // Rockon:works
		restartMusicServices.add("org.abrantes.rockonlite"); // works
		restartMusicServices.add("com.tunewiki.lyricplayer.android:player");// works
		restartMusicServices.add("com.tunewiki.lyricplayer.android");
		restartMusicServices.add("com.nsw.android.mediaexplorer"); // works
		restartMusicServices.add("com.mixzing.basic"); // works

		// testing needed to confirm they are correctly stopped
		restartMusicServices.add("com.streamfurious.android.pro");
		restartMusicServices.add("mobi.beyondpod");
		restartMusicServices
				.add("com.snoggdoggler.android.applications.doggcatcher.v1_0");
		restartMusicServices.add("com.android.DroidLiveLite");
		restartMusicServices.add("com.android.DroidLiveLite:remote");
		restartMusicServices.add("com.android.DroidLivePlayer");
		restartMusicServices.add("com.android.DroidLivePlayer:remote");
		restartMusicServices.add("com.mediafly.android");
		restartMusicServices.add("com.mediafly.android.video");
		restartMusicServices.add("org.iii.ro.meridian");
		restartMusicServices.add("org.iii.ro.iiivpa");
		restartMusicServices.add("org.iii.romulus.meridian");
		restartMusicServices.add("com.mixzing.basic");
		restartMusicServices.add("com.jadn.cc");
		restartMusicServices.add("com.jadn.cc:remote");
		restartMusicServices.add("com.slacker.radio"); // Slacker Radio
		restartMusicServices.add("com.leadapps.android.radio"); // A Online
																// Radio<<
		restartMusicServices.add("com.crossforward.audiobooks"); // Crossforward
		restartMusicServices.add("com.yyqidian.musiconlinelite"); // Music
																	// online
																	// lite
		restartMusicServices.add("com.amblingbooks.bookplayerlite");
		restartMusicServices.add("com.amblingbooks.bookplayerpersonal");
		restartMusicServices.add("com.amblingbooks.bookplayerpro");
		restartMusicServices.add("de.stohelit.folderplayer:remote"); // MortPlayer
																		// Music
																		// (beta)
		restartMusicServices.add("com.maplsroid.mplayer.activity:remote"); // MaplePlayer
																			// v1.5.0
		restartMusicServices.add("com.variamobile.soundwave"); // Rhapsody
	
		restartMusicServices.add("com.droidbolts.dpod"); // Dpod

		return restartMusicServices;
	}

	private String getOwnService() {
		// Get own set service
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		String ownService = settings
				.getString(SleepTimer.PREFS_OWN_SERVICE, "");
		return ownService;
	}

	public void setVolumeBack(int oldMusicVolumeLevel) {
		Intent newIntent = new Intent(this, SetVolumeBackReceiver.class);
		newIntent.putExtra(SetVolumeBackReceiver.INTENT_EXTRA_MUSIC_VOLUME,
				oldMusicVolumeLevel);
		PendingIntent sender = PendingIntent
				.getBroadcast(this, 0, newIntent, 0);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.SECOND, 10);

		// Schedule the alarm!
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
	}

	public void killDeadServices() {
		Intent newIntent = new Intent(this, KillDeadServicesReceiver.class);
		PendingIntent sender = PendingIntent
				.getBroadcast(this, 0, newIntent, 0);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.SECOND, 8);

		// Schedule the alarm!
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
