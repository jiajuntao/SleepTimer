package ch.pboos.android.SleepTimer;

import com.nullwire.trace.ExceptionHandler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SetTimeDialog extends Activity {
	private Button buttonInc1;
	private Button buttonDec1;
	private Button buttonInc5;
	private Button buttonDec5;
	private Button buttonSet;
	private SeekBar seekBar;
	private TextView textMinutes;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ExceptionHandler.register(this, "http://pboos.ch/bugs/server.php");
		
		setContentView(R.layout.set_time_dialog);

		buttonInc1 = (Button)findViewById(R.id.ButtonInc1);
		buttonDec1 = (Button)findViewById(R.id.ButtonDec1);
		buttonInc5 = (Button)findViewById(R.id.ButtonInc5);
		buttonDec5 = (Button)findViewById(R.id.ButtonDec5);
		buttonSet = (Button)findViewById(R.id.ButtonSet);
		seekBar = (SeekBar)findViewById(R.id.SeekBar01);
		textMinutes = (TextView)findViewById(R.id.TextViewMinutes);
		
		Bundle b = getIntent().getExtras();
		int minutes = b.getInt("minutes");
		textMinutes.setText(String.format(getResources().getString(R.string.x_minutes),Integer.toString(minutes)));
		seekBar.setProgress(minutes-1);
		
		buttonDec1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(seekBar.getProgress() > 0)
					seekBar.setProgress(seekBar.getProgress()-1);
				
			}
		});
		buttonDec5.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(seekBar.getProgress()-5 >= 0)
					seekBar.setProgress(seekBar.getProgress()-5);
				else
					seekBar.setProgress(0);
				
			}
		});
		
		
		buttonInc1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(seekBar.getProgress() < seekBar.getMax())
					seekBar.setProgress(seekBar.getProgress()+1);
				
			}
		});
		buttonInc5.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(seekBar.getProgress()+5 <= seekBar.getMax())
					seekBar.setProgress(seekBar.getProgress()+5);
				else
					seekBar.setProgress(seekBar.getMax());
				
			}
		});
		
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				textMinutes.setText(String.format(getResources().getString(R.string.x_minutes),Integer.toString(progress+1)));
			}
		});

		buttonSet.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putInt("minutes", seekBar.getProgress()+1);
				Intent mIntent = new Intent();
				mIntent.putExtras(bundle);
				setResult(0, mIntent);
				finish();
			}
		});
	}
}
