package ch.pboos.android.SleepTimer;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;

public class StopMusicReceiver extends BroadcastReceiver 
{
	private static SensorShakeListener listener;

    @Override
    public void onReceive(Context context, Intent intent)
    {
    	int stopMinutes = intent.getExtras().getInt(SleepTimer.INTENT_EXTRA_STOP_MINUTES);
    	stopMinutes--;

        if(stopMinutes>0){
        	postPoneStopMusic(context, stopMinutes);
            
            if(stopMinutes==1){
            	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                boolean doShake = settings.getBoolean("shakeActivated", false);
            	if(doShake){
	            	playSound(context);
	            	
	            	// register shakelistener
	            	listener = new SensorShakeListener(context);
	            	SensorManager m_sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
	            	int sensorIds = m_sensorManager.getSensors();
	            	boolean deviceSupportsAccelerometer = (sensorIds & Sensor.TYPE_ACCELEROMETER)==Sensor.TYPE_ACCELEROMETER;
	            	if(deviceSupportsAccelerometer)
	            		m_sensorManager.registerListener(listener, m_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
            	}
            }
		} else {
			setShakeInactive(context);
			
			SleepTimer.setNotification(context, context.getResources().getString(R.string.notify_goingtosleep), context.getResources().getString(R.string.notify_goingtosleep2));
			Intent i = new Intent(context, StopMusicService.class);
			context.startService(i);
		}	
    }

	private void playSound(Context context) {
		MediaPlayer mp = MediaPlayer.create(context, R.raw.harp);
		mp.start();
	}

	public static void setShakeInactive(Context context) {
		if(listener!=null){
			SensorManager m_sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
			m_sensorManager.unregisterListener(listener);
			listener = null;
		}
	}

	private void postPoneStopMusic(Context context, int stopMinutes) {
		SleepTimer.setNotificationMinutes(context, Integer.toString(stopMinutes));  
		Intent newIntent = new Intent(context, StopMusicReceiver.class);
		newIntent.putExtra(SleepTimer.INTENT_EXTRA_STOP_MINUTES, stopMinutes);
		
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.SECOND, SleepTimer.TIME_BETWEEN_INFO);

		// Schedule the alarm!
		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
	}

    private class SensorShakeListener implements SensorEventListener{
    	private Context context;
		private boolean enteredOnce;
    	
		public SensorShakeListener(Context context) {
			this.context = context;
			enteredOnce=false;
		}

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			// accuracy doesn't matter in this case.
		}
	
		@Override
		public void onSensorChanged(SensorEvent event) {
			if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) 
	        { 				
	             double forceThreshHold = 1.25f; // 1.5 
	              
	             double totalForce = 0.0f;
	             totalForce += Math.pow(event.values[SensorManager.DATA_X]/SensorManager.GRAVITY_EARTH, 2.0); 
	             totalForce += Math.pow(event.values[SensorManager.DATA_Y]/SensorManager.GRAVITY_EARTH, 2.0); 
	             totalForce += Math.pow(event.values[SensorManager.DATA_Z]/SensorManager.GRAVITY_EARTH, 2.0); 
	             totalForce = Math.sqrt(totalForce);
	              
	             if(totalForce > forceThreshHold) //  && (m_totalForcePrev > forceThreshHold)
	             { 
	            	 if(enteredOnce)
	            		 return;
	            	 enteredOnce=true;
	            	 setShakeInactive(context);
	             	 SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
	                 String shakeMinutes= settings.getString("shakeMinutes", "10");
	                 int shakeMinutesAsInt;
	                 try{
	                	 shakeMinutesAsInt = Integer.parseInt(shakeMinutes);
	                 } catch(Exception e){
	                	 shakeMinutesAsInt=10;
	                 }
	            	 postPoneStopMusic(context, shakeMinutesAsInt); // TODO: snoozeMinutes could be made better
	            	 playSound(context);
	             }
	        }
		}
	}
    

}