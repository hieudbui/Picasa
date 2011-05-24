package com.google.api.client.sample.picasa;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import android.accounts.Account;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.sample.picasa.model.AlbumFeed;
import com.google.api.client.sample.picasa.model.PhotoEntry;
import com.google.api.client.sample.picasa.model.PicasaUrl;

public class MyWidgetProvider extends AppWidgetProvider {
	private static final String TAG = "MyWidgetProvider";

	@Override
	public void onEnabled(Context context) {
		Log.d(TAG, "onEnabled " + this.toString());
	}

	protected AccountHelperForWidget getAccountHelper(Context context) {
		PicasaApplication application = getApplication(context);
		return new AccountHelperForWidget(application);
	}

	protected AlbumHelperForWidget getAlbumHelper(Context context) {
		PicasaApplication application = getApplication(context);
		return new AlbumHelperForWidget(application);
	}

	protected PicasaApplication getApplication(Context context) {
		PicasaApplication application = (PicasaApplication) context
				.getApplicationContext();
		return application;
	}

	protected HttpTransport getTransport(Context context) {
		PicasaApplication application = (PicasaApplication) context
				.getApplicationContext();
		return application.getTransport();
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		Log.d(TAG, "onUpdate " + this.toString());
		AccountHelperForWidget accountHelper = getAccountHelper(context);
		Account account = accountHelper.getAccountFromSharedPreferences();

		AlbumHelperForWidget albumHelper = getAlbumHelper(context);
		String album = albumHelper.getAlbumFromSharedPreferences();
		Log.d(TAG, "onUpdate account: " + account + " album: " + album);
		for (int widgetId : appWidgetIds) {
			if (account != null && album != null) {
				try {
					showAlbumImages(context, appWidgetManager, widgetId);
				} catch (Exception e) {
					e.printStackTrace();
					showSelectAlbumView(context, appWidgetManager, widgetId,
							e.getMessage());
				}

			} else {
				showSelectAlbumView(context, appWidgetManager, widgetId, null);
			}
		}
	}

	protected void showAlbumImages(Context context,
			final AppWidgetManager appWidgetManager, final int widgetId)
			throws Exception {
		final RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget_image_layout);
		AlbumFeed albumFeed = getAlbum(context);
		Log.d(TAG, "albumFeed " + albumFeed);

		List<PhotoEntry> photos = albumFeed.photos;
		AsyncImageLoader loader = new AsyncImageLoader();

		for (int i = 0; i < photos.size(); i++) {
			PhotoEntry photo = photos.get(i);
			if (i == 0) {
				String url = photo.mediaGroup.mediaContent.url;
				// String url = photo.mediaGroup.getLargeThumbNail().url;
				Log.d(TAG, "url " + url);
				// Drawable image = loader.loadDrawable(url);
				// if (image != null) {
				// Log.d(TAG, "loading image " + image);
				// // views.setImageViewBitmap(R.id.imageView,
				// // ((BitmapDrawable) image).getBitmap());
				// views.setImageViewResource(R.id.imageView,
				// R.drawable.img_9348);
				// appWidgetManager.updateAppWidget(widgetId, views);
				// Log.d(TAG, "finished loading image " + image);
				// }
				Bitmap image = getImageBitmap(url);
				String fileName = "myImage.jpg";
				FileOutputStream fileOutputStream = context.openFileOutput(
						"myImage.jpg", Context.MODE_WORLD_READABLE );
				BufferedOutputStream bos = new BufferedOutputStream(
						fileOutputStream);
				image.compress(CompressFormat.JPEG, 50, bos);
				bos.flush();
				bos.close();

				views.setImageViewUri(R.id.imageView,
						Uri.fromFile(context.getFileStreamPath(fileName)));
				appWidgetManager.updateAppWidget(widgetId, views);
				// if (image != null) {
				// Log.d(TAG, "loading image " + image);
				// views.setImageViewBitmap(R.id.imageView, image);
				// appWidgetManager.updateAppWidget(widgetId, views);
				// Log.d(TAG, "finished loading image " + image);
				// }
				break;
			}
		}
	}

	private Bitmap getImageBitmap(String url) {
		Bitmap bm = null;
		InputStream is = null;
		BufferedInputStream bis = null;
		try {
			URL aURL = new URL(url);
			URLConnection conn = aURL.openConnection();
			conn.setConnectTimeout(1000);
			conn.connect();
			is = conn.getInputStream();
			bis = new BufferedInputStream(is);
			bm = BitmapFactory.decodeStream(bis);
			// if (bm == null)
			// {
			// Log.d(TAG, "unable to get bitmap for url: " + url);
			// }
			// else
			// {
			// Log.d(TAG, Calendar.getInstance().getTimeInMillis() + " url: " +
			// url + " bitmap: "
			// + bm.toString());
			// }
		} catch (IOException e) {
			// TODO
			// handle this
			e.printStackTrace();
		} finally {
			try {
				if (bis != null) {
					bis.close();
				}

				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				// TODO
				// handle this
				e.printStackTrace();
			}
		}

		return bm;
	}

	protected AlbumFeed getAlbum(Context context) throws IOException {
		AlbumHelperForWidget albumHelper = getAlbumHelper(context);
		AccountHelperForWidget accountHelper = getAccountHelper(context);

		HttpTransport transport = getTransport(context);
		String authToken = accountHelper.getAuthTokenFromSharedPreferences();
		((GoogleHeaders) transport.defaultHeaders).setGoogleLogin(authToken);

		Log.d(TAG, "authToken " + authToken);

		AlbumFeed albumFeed = AlbumFeed.executeGet(getTransport(context),
				new PicasaUrl(albumHelper.getAlbumFromSharedPreferences()));
		return albumFeed;
	}

	protected void showSelectAlbumView(Context context,
			AppWidgetManager appWidgetManager, int widgetId, String text) {
		Log.d(TAG, "showSelectAlbumView text: " + text);
		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget_text_layout);
		if (text != null) {
			views.setTextViewText(R.id.textView, text);
		}
		appWidgetManager.updateAppWidget(widgetId, views);
	}
}
