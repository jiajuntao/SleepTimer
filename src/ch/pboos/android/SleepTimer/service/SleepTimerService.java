package ch.pboos.android.SleepTimer.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ch.pboos.android.SleepTimer.KillDeadServicesReceiver;
import ch.pboos.android.SleepTimer.RootUtils;
import ch.pboos.android.SleepTimer.SetVolumeBackReceiver;
import ch.pboos.android.SleepTimer.SleepTimer;
import ch.pboos.android.SleepTimer.SleepTimerStatus;
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

public class SleepTimerService extends Service {
	int oldMusicVolumeLevel = 0;
	SleepTimerServiceBinder binder = new SleepTimerServiceBinder(this);
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

}
