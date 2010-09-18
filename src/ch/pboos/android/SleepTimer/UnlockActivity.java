package ch.pboos.android.SleepTimer;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class UnlockActivity extends Activity {

	private static final int CHECK_PAYMENT = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.unlock);
		
		if(isAppPayed()){
			setAppToPayed();
			return;
		}
		
		if (isPackageAvailable("ch.pboos.android.SleepTimerPayed")) {
			setAppToPayed();
			return;
		}

		if (isPackageAvailable("ch.pboos.android.SleepTimerPayPal")) {
			Intent intent = new Intent(
					"ch.pboos.android.SleepTimerPayPal.CHECK");
			startActivityForResult(intent, CHECK_PAYMENT);
		}

//		ImageView imgPayed = (ImageView) findViewById(R.id.image_payedapp);
//		imgPayed.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				goToUri("market://details?id=ch.pboos.android.SleepTimerPayed");
//			}
//		});

//		ImageView imgPayPal = (ImageView) findViewById(R.id.image_paypalapp);
//		imgPayPal.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				goToUri("market://details?id=ch.pboos.android.SleepTimerPayPal");
//			}
//		});
		
		ImageView imgPayPal = (ImageView) findViewById(R.id.image_donate);
		imgPayPal.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				goToUri("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=BAZUNJ8H2QN64&lc=CH&item_name=SleepTimer&item_number=sleeptimer&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted");
			}
		});
	}

	private void goToUri(String uriString) {
		Uri uri = Uri.parse(uriString);  
		Intent it = new Intent(Intent.ACTION_VIEW, uri);  
		startActivity(it);
	}

	protected void setAppToPayed() {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(getString(R.string.attr_ispayed), true);
		editor.commit();
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public boolean isPackageAvailable(String action) {
		final PackageManager packageManager = getPackageManager();
		Intent intent = packageManager.getLaunchIntentForPackage(action);

		if (intent == null)
			return false;

		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case RESULT_OK:
			setAppToPayed();
		default:
			break;
		}
	}

	private boolean isAppPayed() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        return settings.getBoolean(getString(R.string.attr_ispayed), false);
	}
}
