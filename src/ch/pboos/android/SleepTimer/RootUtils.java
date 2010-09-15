package ch.pboos.android.SleepTimer;

import java.io.IOException;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class RootUtils {

	private static final String TAG = "SleepTimer";

	@SuppressWarnings("unused")
	public static boolean hasRoot(Context context, boolean showToast) {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec("su");
		} catch (IOException e) {
			messageNoRoot(context, e, showToast);
			return false;
		} catch (java.lang.RuntimeException e) {
			messageNoRoot(context, e, showToast);
			return false;
		}

		Log.i(TAG, "This ROM does allow root access");
		return true;
	}

	private static void messageNoRoot(Context c, Exception e, boolean showToast) {
		Log.e(TAG, "This ROM does not allow root access");
		if(showToast) {
			Toast.makeText(c, c.getString(R.string.message_froyo_toast_noroot),
				Toast.LENGTH_LONG).show();
		}
		e.printStackTrace();
	}
}
