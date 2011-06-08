package com.google.api.client.sample.picasa;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;

public class MyWidgetProvider extends AppWidgetProvider {
	private static final String TAG = "MyWidgetProvider";
	public static final String UPDATE_WIDGET = "updateWidget";
	public static final int UPDATE_RATE = 60000;

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.d(TAG, "onReceived " + this.toString() + "action " + action);
		if (action != null && action.equals(UPDATE_WIDGET)) {
			final AppWidgetManager manager = AppWidgetManager
					.getInstance(context);
			onUpdate(context, manager,
					manager.getAppWidgetIds(new ComponentName(context,
							MyWidgetProvider.class)));
		} else {
			super.onReceive(context, intent);
		}
	}

	@Override
	public void onEnabled(Context context) {
		Log.d(TAG, "onEnabled " + this.toString());
	}

	@Override
	public void onDisabled(Context context) {
		Log.d(TAG, "onDisabled " + this.toString());
		context.stopService(new Intent(context, WidgetService.class));
		super.onDisabled(context);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		for (int appWidgetId : appWidgetIds) {
			setAlarm(context, appWidgetId, -1);
		}
		super.onDeleted(context, appWidgetIds);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		Log.d(TAG, "onUpdate " + this.toString());
		for (int appWidgetId : appWidgetIds) {
			setAlarm(context, appWidgetId, UPDATE_RATE);
		}
		super.onUpdate(context, appWidgetManager, appWidgetIds);

	}

	public static PendingIntent makeWidgetServicePendingIntent(Context context,
			String command, int appWidgetId, int position) {
		Intent active = new Intent(context, WidgetService.class);
		active.setAction(command);

		active.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		// this Uri data is to make the PendingIntent unique, so it wont be
		// updated by FLAG_UPDATE_CURRENT
		// so if there are multiple widget instances they wont override each
		// other
		Uri data = Uri.withAppendedPath(
				Uri.parse("countdownwidget://widget/id/#" + command
						+ appWidgetId), String.valueOf(appWidgetId));
		active.setData(data);
		if (position >= 0) {
			active.putExtra("position", position);
		}
		return (PendingIntent.getService(context, 0, active,
				PendingIntent.FLAG_UPDATE_CURRENT));
	}

	public static void setAlarm(Context context, int appWidgetId, int updateRate) {
		PendingIntent newPending = makeWidgetServicePendingIntent(context,
				WidgetService.UPDATE, appWidgetId, -1);

		AlarmManager alarms = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		if (updateRate >= 0) {

			alarms.setRepeating(AlarmManager.ELAPSED_REALTIME,
					SystemClock.elapsedRealtime(), updateRate, newPending);

		} else {
			// on a negative updateRate stop the refreshing
			alarms.cancel(newPending);
		}
	}
}
