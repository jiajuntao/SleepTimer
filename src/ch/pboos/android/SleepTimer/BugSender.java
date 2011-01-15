package ch.pboos.android.SleepTimer;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

public class BugSender {
	public static void sendBug(Context context, String name, String text) {
		HttpClient httpclient = new DefaultHttpClient();
		// Your URL
		HttpPost httppost = new HttpPost("http://pboos.ch/bugs/server.php");

		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			// Your DATA
			nameValuePairs.add(new BasicNameValuePair("manufacturer", Build.MANUFACTURER));
			nameValuePairs.add(new BasicNameValuePair("model", Build.MODEL));
			nameValuePairs.add(new BasicNameValuePair("device", Build.DEVICE));
			nameValuePairs.add(new BasicNameValuePair("product", Build.PRODUCT));
			nameValuePairs.add(new BasicNameValuePair("build", Build.FINGERPRINT));
			nameValuePairs.add(new BasicNameValuePair("android_version_release", Build.VERSION.RELEASE));
			nameValuePairs.add(new BasicNameValuePair("android_version_sdk_int", Integer.toString(Build.VERSION.SDK_INT)));

			nameValuePairs.add(new BasicNameValuePair("package_version", getSoftwareVersion(context)));
			nameValuePairs.add(new BasicNameValuePair("package_name", getSoftwareName(context)));
			String stacktrace = name + "\n\n"+text + "\n\n"+getCurrentStackTrace();
			nameValuePairs.add(new BasicNameValuePair("stacktrace", stacktrace));

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

//			HttpResponse response;
//			response = httpclient.execute(httppost);
			httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static String getCurrentStackTrace() {
		final Writer result = new StringWriter();
	    final PrintWriter printWriter = new PrintWriter(result);
	    new Exception().printStackTrace(printWriter);
	    return result.toString();
	    
		//return Thread.currentThread().getStackTrace();
	}

	private static String getSoftwareVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			Log.e("BugSender", "Package name not found", e);
		}
		return "";
	}

	private static String getSoftwareName(Context context) {
		return context.getPackageName();
	}
}
