package ch.pboos.android.SleepTimer.StopConnections;

import ch.pboos.android.SleepTimer.DeadServicesKiller;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.DeadObjectException;
import android.os.IBinder;

public class HtcMediaPlayerStopperServiceConnection implements ServiceConnection {
	public com.htc.music.IMediaPlaybackService mService;
	@Override
	public void onServiceConnected(ComponentName name,
			IBinder service) {
		mService = com.htc.music.IMediaPlaybackService.Stub.asInterface(service);
		try {
			if (mService.isPlaying()) {
				mService.pause();
			}
		} catch(DeadObjectException e){
			DeadServicesKiller.addDeadService("com.htc.music");
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