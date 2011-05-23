package com.google.api.client.sample.picasa;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {
	private static final String TAG = "MyWidgetProvider";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		PicasaApplication picasaApplication=(PicasaApplication)context.getApplicationContext();
		for (int widgetId : appWidgetIds) {
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout);
			views.setImageViewResource(R.id.imageView,
					R.drawable.sample_thumb_0);
			appWidgetManager.updateAppWidget(widgetId, views);
			Log.d(TAG, "update called for widgetId: " + widgetId);
		}
	}
}
