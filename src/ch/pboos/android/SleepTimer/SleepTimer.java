package ch.pboos.android.SleepTimer;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import ch.pboos.android.SleepTimer.service.SleepTimerService;
import ch.pboos.android.SleepTimer.service.SleepTimerServiceBinder;

import com.nullwire.trace.ExceptionHandler;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

public class SleepTimer extends Activity {
	public static final String PREFS_OWN_SERVICE = "ownService";
	public static final String PREFS_MINUTES = "minutes";
	public static final String INTENT_EXTRA_STOP_MINUTES = "stopMinutes";
	public static final int TIME_BETWEEN_INFO = 60; // TODO: set time again to 60
	private static final int SLEEP_TIMER_NOTIFICATION_ID = 47319;
	protected static final String PREFS_MUSIC_PLAYER = "pref_musicapp";
	protected static final String PREFS_MUSIC_PLAYER_NAME = "pref_music_app_name";
	private static final String PREFS_INFO_VERSION = "pref_info_version";
	
	private Button buttonStartStop;
	private Button buttonStartPlayer;
	private Button buttonSetMinutes;

	private int sleep_minutes;
	
	private final Handler mHandler = new Handler();

	SleepTimerServiceBinder sleepTimerService;
	SleepTimerCallback sleepTimerCallback = new SleepTimerCallback(SleepTimer.this, mHandler, sleepTimerService);
	ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i("SleepTimer", "Service bound");
			sleepTimerService = (SleepTimerServiceBinder) service;
			sleepTimerService.registerCallback(sleepTimerCallback);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			Log.i("XtraZone", "Service unbound");
			sleepTimerService = null;
		}
	};
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
//        SOMABanner.setPubID("923826534");
//        SOMABanner.setAdID("65731726");
        
        ExceptionHandler.register(this, "http://pboos.ch/bugs/server.php");
        
        setContentView(R.layout.main);
                
        buttonStartStop = (Button)findViewById(R.id.ButtonStart);
        buttonStartPlayer = (Button)findViewById(R.id.Button_StartPlayer);
        buttonSetMinutes = (Button)findViewById(R.id.ButtonTime);
        
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        sleep_minutes = settings.getInt(PREFS_MINUTES, 5);
        setButtonMinutes(sleep_minutes);
        
        buttonStartPlayer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(SleepTimer.this);
		        String musicPlayerPackage = settings.getString(PREFS_MUSIC_PLAYER, "");
				Intent musicPlayerIntent = getPackageManager().getLaunchIntentForPackage(musicPlayerPackage);
		    	if(musicPlayerIntent==null){
		    		 Toast mToast = Toast.makeText(SleepTimer.this, SleepTimer.this.getResources().getString(R.string.pref_music_player_not_set),
			                    Toast.LENGTH_LONG);
			            mToast.show();
		    	} else {
					Log.d("START", musicPlayerPackage);
					try {
						startActivity(musicPlayerIntent);
					} catch(ActivityNotFoundException e){
						String newMusicPlayerPackage = musicPlayerPackage;
						newMusicPlayerPackage = newMusicPlayerPackage.replace("/com.android.internal.app", "");
						if(newMusicPlayerPackage.equals("com.android.music")){
							musicPlayerIntent = new Intent("com.android.music.PLAYBACK_VIEWER");
							startActivity(musicPlayerIntent);
						} else {
							Toast mToast = Toast.makeText(SleepTimer.this, SleepTimer.this.getResources().getString(R.string.error_cannot_start_app),
				                    Toast.LENGTH_LONG);
				            mToast.show();
							BugSender.sendBug(SleepTimer.this, "Launch Button - ActivityNotFoundException", "Tried to start "+musicPlayerPackage);
						}
					}
		    	}
			}
		});
        buttonSetMinutes.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(SleepTimer.this, SetTimeDialog.class);
				i.putExtra("minutes", sleep_minutes);
				startActivityForResult(i, 0);
			}
		});
        
        buttonStartStop.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(!SleepTimerStatus.isRunning(SleepTimer.this)){
					startSleepTimer();
				} else {
					stopSleepTimer();
				}
			}
        });
        
        
        final int appVersion = getAppVersionCode();

        int lastAppVersion = settings.getInt(PREFS_INFO_VERSION, 0);
        if(Integer.valueOf(Build.VERSION.SDK)>7 && lastAppVersion < appVersion){
        	Toast.makeText(this, getString(R.string.message_froyo_checkroot), Toast.LENGTH_LONG);
        	
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	if(RootUtils.hasRoot(this, false)) {
	        	builder.setTitle(getString(R.string.message_froyo_and_higher_title))
        		   .setMessage(getString(R.string.message_froyo_root))
        	       .setCancelable(false)
        	       .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   savePreferences(PREFS_INFO_VERSION, appVersion); // to give root
			        	   try {
			        		   Runtime.getRuntime().exec("su");
			        	   } catch (IOException e) {
			        		   e.printStackTrace();
			        	   }
			        	   dialog.cancel();
			           }
        	       });
        	} else {
        		builder.setTitle(getString(R.string.message_froyo_and_higher_title))
     		   .setMessage(getString(R.string.message_froyo_noroot))
     	       .setCancelable(false)
     	       .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   savePreferences(PREFS_INFO_VERSION, appVersion); // to give root
			        	   dialog.cancel();
			           }
     	       });
    		}
        	AlertDialog alert = builder.create();
        	alert.show();
        }
        
        connectToService();
    }

	private void connectToService() {
		Intent intent = new Intent(this, SleepTimerService.class);
		startService(intent);
		bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (sleepTimerService != null) {
			sleepTimerService.unregisterCallback(sleepTimerCallback);
		}
		unbindService(serviceConnection);
	}

	private int getAppVersionCode() {
		PackageInfo pInfo = null;

        try {
	        pInfo = getPackageManager().getPackageInfo("ch.pboos.android.SleepTimer", PackageManager.GET_META_DATA);

        } catch (NameNotFoundException e) {
        	return 0;
        }
        
		return pInfo.versionCode;
	}
    
    private void stopSleepTimer() {
		Intent intent = new Intent(SleepTimer.this, StopMusicReceiver.class);
		intent.putExtra(INTENT_EXTRA_STOP_MINUTES, 0);
		PendingIntent sender = PendingIntent.getBroadcast(SleepTimer.this,
		        0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		// And cancel the alarm.
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.cancel(sender);
		stopNotification(SleepTimer.this);
		
		SleepTimerStatus.setRunning(this, false);
		setStartStopButtonText();
		
		StopMusicReceiver.setShakeInactive(SleepTimer.this);
	}


	public static void setNotificationMinutes(Context context, String minutes) {
		setNotification(context, minutes+" "+context.getResources().getString(R.string.notify_minutes_left), minutes+" "+context.getResources().getString(R.string.notify_minutes_left_until_sleep));
	}
    
	public static void setNotification(Context context, String text, String additional){
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
		
		// Short Information
		int icon = R.drawable.sleep_icon;
		CharSequence tickerText = text;
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
		
		// Extended Information
		CharSequence contentTitle = "Sleep Timer";
		CharSequence contentText = additional;
		Intent notificationIntent = new Intent(context, SleepTimer.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		
		// Pass to NotificationManager
		mNotificationManager.notify(SLEEP_TIMER_NOTIFICATION_ID, notification);
	}
	
	public static void stopNotification(Context context){
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
		mNotificationManager.cancel(SLEEP_TIMER_NOTIFICATION_ID);
	}
	
	/* Creates the menu items */
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.options_menu, menu);
	    return true;
	}
	
	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.menu_settings:
	    	Intent settingsIntent = new Intent(this, Preferences.class);
	    	startActivity(settingsIntent);
	    	return true;
	    case R.id.menu_changelog:
	    	Intent i = new Intent(this, ChangeLog.class);
	    	startActivity(i);
	        return true;
	    case R.id.menu_feedback:
	    	//Context mContext = getApplicationContext();
	    	final Dialog dialog = new Dialog(this);

	    	dialog.setContentView(R.layout.feedback_dialog);
	    	dialog.setTitle(getResources().getString(R.string.dialog_feedback_title));	        
	    	Button btnSend = (Button)dialog.findViewById(R.id.Button_send);
	    	btnSend.setOnClickListener(new OnClickListener() {			
				@Override
				public void onClick(View v) {
					String sendText = "";
					CheckBox box = (CheckBox)dialog.findViewById(R.id.CheckBox_services);
					if(box.isChecked()){
						sendText += getResources().getString(R.string.dialog_feedback_player_name)+"<br/><br/>";
						sendText += "Services:<br/>";
						ActivityManager am = (ActivityManager)SleepTimer.this.getSystemService(ACTIVITY_SERVICE);
						List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(50);
						for (int i=0; i<rs.size(); i++) {
							ActivityManager.RunningServiceInfo rsi = rs.get(i);
							sendText += rsi.process+" - "+rsi.service.getClassName()+"<br/>";
						}
					}
					
					Intent mailIntent = new Intent(Intent.ACTION_SEND);
			    	mailIntent.setType("text/plain");
			    	mailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"mail@pboos.ch"});
			    	mailIntent.putExtra(Intent.EXTRA_SUBJECT, "SleepTimer Feedback/Bug/Request");
			    	mailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(sendText));
			    	startActivity(Intent.createChooser(mailIntent, "Select e-mail application"));
			    	startActivity(mailIntent);
			    	dialog.dismiss();
				}
			});
	    	dialog.show();
	        return true;
	    case R.id.menu_donate:
	    	Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=BAZUNJ8H2QN64&lc=CH&item_name=SleepTimer&item_number=sleeptimer&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted"));  
	    	startActivity(viewIntent);
	        return true;
		case R.id.menu_quit:
			this.finish();
			return true;
    	}
	    return false;
	}
	
	
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(data!=null && data.hasExtra("minutes")){
	    	sleep_minutes = data.getIntExtra("minutes", sleep_minutes);
	    	savePreferences(PREFS_MINUTES, sleep_minutes);
	        setButtonMinutes(sleep_minutes);
    	}
	}

	private void savePreferences(String name, int value) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(name, value);
		editor.commit();
	}
    
    private void setButtonMinutes(int min){
    	buttonSetMinutes.setText(String.format(getResources().getString(R.string.x_minutes),Integer.toString(sleep_minutes)));
    }

	private void startSleepTimer() {
		if(sleep_minutes<1){
			sleep_minutes=1;
			setButtonMinutes(sleep_minutes);
		}
		
		Intent intent = new Intent(SleepTimer.this, StopMusicReceiver.class);
		intent.putExtra(INTENT_EXTRA_STOP_MINUTES, sleep_minutes);
		
		PendingIntent sender = PendingIntent.getBroadcast(SleepTimer.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.SECOND, TIME_BETWEEN_INFO);

		// Schedule the alarm!
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);

		// Tell the user about what we did.
		setNotificationMinutes(SleepTimer.this, Integer.toString(sleep_minutes));
		Toast mToast = Toast.makeText(SleepTimer.this, String.format(SleepTimer.this.getResources().getString(R.string.notify_going_to_sleep_in), Integer.toString(sleep_minutes)),
		        Toast.LENGTH_LONG);
		mToast.show();
		
		SleepTimerStatus.setRunning(this, true);
		setStartStopButtonText();
	}

	@Override
	protected void onResume() {
		super.onResume();
		setStartStopButtonText();
		Button startPlayerButton = (Button)findViewById(R.id.Button_StartPlayer);
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(SleepTimer.this);
        String musicPlayerName = settings.getString(PREFS_MUSIC_PLAYER_NAME, "");
        if(musicPlayerName.equals(""))
        	musicPlayerName = "???";
        String buttonText = String.format(getResources().getString(R.string.button_start_player), musicPlayerName);
		startPlayerButton.setText(buttonText);
	}

	private void setStartStopButtonText() {
		setStartStopButtonText(SleepTimerStatus.isRunning(this));
	}
	
	private void setStartStopButtonText(boolean isRunning){
		Button startStopButton = (Button)findViewById(R.id.ButtonStart);
		if(isRunning){
			startStopButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_delete, 0, 0, 0);
			startStopButton.setText(R.string.button_stop_sleeptimer);
		} else {
			startStopButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_play, 0, 0, 0);
			startStopButton.setText(R.string.button_start_sleeptimer);
		}
	}
	
	
}