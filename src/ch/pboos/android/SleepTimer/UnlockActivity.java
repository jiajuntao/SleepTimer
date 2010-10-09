package ch.pboos.android.SleepTimer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class UnlockActivity extends Activity {

	private static final int CHECK_PAYMENT = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.unlock);
		
		if(UnlockTools.isAppPayed(this)){
			UnlockTools.setAppToPayed(this);
			finish();
			return;
		}
		
		if(UnlockTools.isPaidPackageInstalled(this)){
			UnlockTools.setAppToPayed(this);
			finish();
			return;
		}

		if (UnlockTools.isPackageAvailable(this, "ch.pboos.android.SleepTimerPayPal")) {
			Intent intent = new Intent(
					"ch.pboos.android.SleepTimerPayPal.CHECK");
			startActivityForResult(intent, CHECK_PAYMENT);
		}

		ImageView imgPayed = (ImageView) findViewById(R.id.image_paidapp);
		imgPayed.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				goToUri("market://details?id=ch.pboos.android.SleepTimerPaid");
			}
		});

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

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case RESULT_OK:
			UnlockTools.setAppToPayed(this);
			finish();
			break;
		default:
			break;
		}
	}
}
