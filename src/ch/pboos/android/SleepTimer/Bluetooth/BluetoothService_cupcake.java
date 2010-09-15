package ch.pboos.android.SleepTimer.Bluetooth;

//From: http://code.google.com/p/android-wifi-tether/source/browse/#svn/trunk/src/android/tether/system

import java.lang.reflect.Method;

import android.app.Application;
import android.content.Context;

public class BluetoothService_cupcake extends BluetoothService {

        Context context = null;
        
        /*
         * Bluetooth API is not exposed publicly, so we need to use reflection
         * to query and set the configuration.
         */
        @SuppressWarnings("unchecked")
        private Object callBluetoothMethod(String methodName) {
	        Object manager = this.context.getSystemService("bluetooth");
	        if(manager==null)
	        	return false;
	        Class c = manager.getClass();
	        Object returnValue = null;
	        if (c == null) {
	                // Nothing
	        } else {
				try {
				    Method enable = c.getMethod(methodName);
				    enable.setAccessible(true);
				    returnValue = enable.invoke(manager);
				} catch (Exception e){
					e.printStackTrace();
				} 
			}
	        return returnValue;
        }
        
        @Override
        public boolean isBluetoothEnabled() {
                return (Boolean) callBluetoothMethod("isEnabled");
        }

        @Override
        public boolean startBluetooth() {
                boolean connected = false;
                callBluetoothMethod("enable");
                int checkcounter = 0;
                while (connected == false && checkcounter <= 60) {
                        // Wait up to 60s for bluetooth to come up.
                        // pand does not behave unless started after BT is enabled.
                        connected = (Boolean) callBluetoothMethod("isEnabled");
                        if (connected == false) {
                                checkcounter++;
                                try {
                                        Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                        // Nothing
                                }
                        } else {
                                break;
                        }
                }
                return connected;
        }

        @Override
        public boolean stopBluetooth() {
                callBluetoothMethod("disable");
                return true;
        }

        @Override
        public void setContext(Context context) {
                this.context = context;
        }
}
