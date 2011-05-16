package com.google.api.client.sample.picasa;


import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		for (int widgetId : appWidgetIds) {
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout);
			views.setImageViewResource(R.id.imageView,R.drawable.sample_thumb_0);
			appWidgetManager.updateAppWidget(widgetId, views);
		}
	}
}
