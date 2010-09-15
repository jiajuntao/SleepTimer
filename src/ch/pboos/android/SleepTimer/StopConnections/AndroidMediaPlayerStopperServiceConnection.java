package ch.pboos.android.SleepTimer.StopConnections;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.DeadObjectException;
import android.os.IBinder;

import ch.pboos.android.SleepTimer.KillDeadServicesReceiver;

import com.android.music.IMediaPlaybackService;

public class AndroidMediaPlayerStopperServiceConnection implements ServiceConnection {
	public IMediaPlaybackService mService;
	@Override
	public void onServiceConnected(ComponentName name,
			IBinder service) {
		mService = IMediaPlaybackService.Stub.asInterface(service);
		try {
			if (mService.isPlaying()) {
				mService.pause();
			}
		} catch(DeadObjectException e){
			KillDeadServicesReceiver.addDeadService("com.android.music");
			return;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
	}
}
