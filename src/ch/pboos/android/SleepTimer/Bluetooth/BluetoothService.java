package ch.pboos.android.SleepTimer.Bluetooth;

// From: http://code.google.com/p/android-wifi-tether/source/browse/#svn/trunk/src/android/tether/system

import android.content.Context;
import android.os.Build;

public abstract class BluetoothService {

    public abstract boolean startBluetooth();
    public abstract boolean stopBluetooth();
    public abstract boolean isBluetoothEnabled();
    public abstract void setContext(Context application);
    
    private static BluetoothService bluetoothService;
    
    public static BluetoothService getInstance() {
        if (bluetoothService == null) {
            String className;

            int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
            if (sdkVersion < 5) {
                className = "ch.pboos.android.SleepTimer.Bluetooth.BluetoothService_cupcake";
            } else {
                className = "ch.pboos.android.SleepTimer.Bluetooth.BluetoothService_eclair";
            }
    
            try {
                Class<? extends BluetoothService> clazz = Class.forName(className).asSubclass(BluetoothService.class);
                bluetoothService = clazz.newInstance();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return bluetoothService;
    }
}
