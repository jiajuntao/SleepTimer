package ch.pboos.android.SleepTimer;

import ch.pboos.android.SleepTimer.service.SleepTimerService;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

public class SleepTimerWidgetProvider extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		Intent intent = new Intent(context, SleepTimerService.class);
		intent.putExtra(SleepTimerService.EXTRA_ACTION, SleepTimerService.ACTION_UPDATE);
		context.startService(intent);
	}
}
