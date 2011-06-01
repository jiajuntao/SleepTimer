
package ch.pboos.android.SleepTimer;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import ch.pboos.android.SleepTimer.service.SleepTimerService;
import ch.pboos.android.SleepTimer.service.SleepTimerServiceBinder;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.nullwire.trace.ExceptionHandler;

public class SleepTimer extends Activity {
    public static final String PREFS_OWN_SERVICE = "ownService";
    public static final String PREFS_MINUTES = "minutes";
    public static final String INTENT_EXTRA_STOP_MINUTES = "stopMinutes";
    protected static final String PREFS_MUSIC_PLAYER = "pref_musicapp";
    protected static final String PREFS_MUSIC_PLAYER_NAME = "pref_music_app_name";
    private static final String PREFS_INFO_VERSION = "pref_info_version";

    private Button buttonStartStop;
    private Button buttonStartPlayer;
    private Button buttonSetMinutes;

    private int sleep_minutes;

    private final Handler mHandler = new Handler();

    SleepTimerServiceBinder sleepTimerService;
    SleepTimerCallback sleepTimerCallback = new SleepTimerCallback(SleepTimer.this, mHandler,
            sleepTimerService);
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("SleepTimer", "Service bound");
            sleepTimerService = (SleepTimerServiceBinder) service;
            sleepTimerService.registerCallback(sleepTimerCallback);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.i("SleepTimer", "Service unbound");
            sleepTimerService = null;
        }
    };
    private AdView adView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (UnlockTools.isPaidPackageInstalled(this))
            UnlockTools.setAppToPayed(this);

        ExceptionHandler.register(this, "http://pboos.ch/bugs/server.php");

        setContentView(R.layout.main);

        if (!UnlockTools.isAppPayed(this)) {
            adView = new AdView(this, AdSize.BANNER, "a14b5487eec65c9");
            LinearLayout layout = (LinearLayout) findViewById(R.id.LayoutTop);
            layout.addView(adView);
            adView.loadAd(new AdRequest());
        }

        buttonStartStop = (Button) findViewById(R.id.ButtonStart);
        buttonStartPlayer = (Button) findViewById(R.id.Button_StartPlayer);
        buttonSetMinutes = (Button) findViewById(R.id.ButtonTime);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        sleep_minutes = settings.getInt(PREFS_MINUTES, 5);
        setButtonMinutes(sleep_minutes);

        buttonStartPlayer.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                SharedPreferences settings = PreferenceManager
                        .getDefaultSharedPreferences(SleepTimer.this);
                String musicPlayerPackage = settings.getString(PREFS_MUSIC_PLAYER, "");
                Intent musicPlayerIntent = getPackageManager().getLaunchIntentForPackage(
                        musicPlayerPackage);
                if (musicPlayerIntent == null) {
                    Toast mToast = Toast.makeText(SleepTimer.this, SleepTimer.this.getResources()
                            .getString(R.string.pref_music_player_not_set), Toast.LENGTH_LONG);
                    mToast.show();
                } else {
                    Log.d("START", musicPlayerPackage);
                    try {
                        startActivity(musicPlayerIntent);
                    } catch (ActivityNotFoundException e) {
                        String newMusicPlayerPackage = musicPlayerPackage;
                        newMusicPlayerPackage = newMusicPlayerPackage.replace(
                                "/com.android.internal.app", "");
                        if (newMusicPlayerPackage.equals("com.android.music")) {
                            musicPlayerIntent = new Intent("com.android.music.PLAYBACK_VIEWER");
                            startActivity(musicPlayerIntent);
                        } else {
                            Toast mToast = Toast.makeText(SleepTimer.this, SleepTimer.this
                                    .getResources().getString(R.string.error_cannot_start_app),
                                    Toast.LENGTH_LONG);
                            mToast.show();
                            BugSender.sendBug(SleepTimer.this,
                                    "Launch Button - ActivityNotFoundException", "Tried to start "
                                            + musicPlayerPackage);
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

        buttonStartStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sleepTimerService.isRunning()) {
                    stopSleepTimer();
                } else {
                    startSleepTimer();
                }
            }
        });

        final int appVersion = getAppVersionCode();

        int lastAppVersion = settings.getInt(PREFS_INFO_VERSION, 0);
        if (lastAppVersion < appVersion) {
            if (Integer.valueOf(Build.VERSION.SDK) > 7) {
                Toast.makeText(this, getString(R.string.message_froyo_checkroot), Toast.LENGTH_LONG);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                if (RootUtils.hasRoot(this, false)) {
                    builder.setTitle(getString(R.string.message_froyo_and_higher_title))
                            .setMessage(getString(R.string.message_froyo_root))
                            .setCancelable(false)
                            .setPositiveButton(R.string.button_ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            savePreferences(PREFS_INFO_VERSION, appVersion); // to
                                                                                             // give
                                                                                             // root
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
                            .setPositiveButton(R.string.button_ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            savePreferences(PREFS_INFO_VERSION, appVersion); // to
                                                                                             // give
                                                                                             // root
                                            dialog.cancel();
                                        }
                                    });
                }
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    private void connectToService() {
        Log.i("SleepTimer", "Connecting to Service");
        Intent intent = new Intent(this, SleepTimerService.class);
        startService(intent);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (sleepTimerService != null) {
            if (!sleepTimerService.isRunning()) {
                Intent intent = new Intent(this, SleepTimerService.class);
                stopService(intent);
            }
            sleepTimerService.unregisterCallback(sleepTimerCallback);
            unbindService(serviceConnection);
            sleepTimerService = null;
        }
    }

    private int getAppVersionCode() {
        PackageInfo pInfo = null;

        try {
            pInfo = getPackageManager().getPackageInfo("ch.pboos.android.SleepTimer",
                    PackageManager.GET_META_DATA);

        } catch (NameNotFoundException e) {
            return 0;
        }

        return pInfo.versionCode;
    }

    private void stopSleepTimer() {
        sleepTimerService.stop();
    }

    /* Creates the menu items */
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (UnlockTools.isAppPayed(this))
            menu.findItem(R.id.menu_unlock).setVisible(false);
        else
            menu.findItem(R.id.menu_feedback).setVisible(false);

        return super.onPrepareOptionsMenu(menu);
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
                PackageManager packageManager = getPackageManager();
                String version = "";
                try {
                    PackageInfo info = packageManager.getPackageInfo("ch.pboos.android.SleepTimer",
                            0);
                    version = info.versionName;
                } catch (NameNotFoundException e) {
                }

                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                String[] recipients = new String[] {
                        "mail@pboos.ch", "",
                };
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                        "SleepTimer Feedback/Bug/Request (" + version + ")");
                emailIntent.setType("text/plain");
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
                return true;
            case R.id.menu_unlock:
                Intent unlockIntent = new Intent(this, UnlockActivity.class);
                startActivity(unlockIntent);
                return true;
            case R.id.menu_donate:
                Intent viewIntent = new Intent(
                        "android.intent.action.VIEW",
                        Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=BAZUNJ8H2QN64&lc=CH&item_name=SleepTimer&item_number=sleeptimer&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted"));
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
        if (data != null && data.hasExtra("minutes")) {
            sleep_minutes = data.getIntExtra("minutes", sleep_minutes);
            savePreferences(PREFS_MINUTES, sleep_minutes);
            setButtonMinutes(sleep_minutes);
            if (sleepTimerService != null) {
                sleepTimerService.updateWidgets();
            } else {
                Intent intent = new Intent(this, SleepTimerService.class);
                intent.putExtra(SleepTimerService.EXTRA_ACTION, SleepTimerService.ACTION_UPDATE);
                startService(intent);
            }
        }
    }

    private void savePreferences(String name, int value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(name, value);
        editor.commit();
    }

    private void setButtonMinutes(int min) {
        buttonSetMinutes.setText(String.format(getResources().getString(R.string.x_minutes),
                Integer.toString(sleep_minutes)));
    }

    private void startSleepTimer() {
        if (sleep_minutes < 0) {
            sleep_minutes = 0;
            setButtonMinutes(sleep_minutes);
        }

        if (sleepTimerService != null) {
            sleepTimerService.start(sleep_minutes);
        } else {
            BugSender.sendBug(this, "Service not connected!",
                    "When trying to start the SleepTimer.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        connectToService();

        Button startPlayerButton = (Button) findViewById(R.id.Button_StartPlayer);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(SleepTimer.this);
        String musicPlayerName = settings.getString(PREFS_MUSIC_PLAYER_NAME, "");
        if (musicPlayerName.equals(""))
            musicPlayerName = "???";
        String buttonText = String.format(getResources().getString(R.string.button_start_player),
                musicPlayerName);
        startPlayerButton.setText(buttonText);

        if (UnlockTools.isAppPayed(this)) {
            ImageView title = (ImageView) findViewById(R.id.image_title);
            title.setImageResource(R.drawable.title_paid);
        } else {
            adView.loadAd(new AdRequest());
        }
    }

    private void setStartStopButtonText(int sleepTimerState) {
        Button startStopButton = (Button) findViewById(R.id.ButtonStart);
        if (sleepTimerState == SleepTimerService.STATE_STOPPED) {
            startStopButton.setCompoundDrawablesWithIntrinsicBounds(
                    android.R.drawable.ic_media_play, 0, 0, 0);
            startStopButton.setText(R.string.button_start_sleeptimer);
        } else {
            startStopButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_delete,
                    0, 0, 0);
            startStopButton.setText(R.string.button_stop_sleeptimer);
        }
    }

    public void updateSleepTimerState(int state, int minutes) {
        setStartStopButtonText(state);
        if (state == SleepTimerService.STATE_SHUTTING_DOWN) {
            finish();
        }
    }

}
