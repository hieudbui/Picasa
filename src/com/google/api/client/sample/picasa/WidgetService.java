package com.google.api.client.sample.picasa;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.sample.picasa.model.AlbumFeed;
import com.google.api.client.sample.picasa.model.PhotoEntry;
import com.google.api.client.sample.picasa.model.PicasaUrl;

public class WidgetService extends Service {
	private static final String TAG = "WidgetService";

	public static final String UPDATE = "update";
	public static final String FORWARD = "forward";
	public static final String SHOW_BUTTON = "show";
	public static final String BACKWARD = "backward";

	protected PicasaApplication getApplication(Context context) {
		PicasaApplication application = (PicasaApplication) context
				.getApplicationContext();
		return application;
	}

	protected AlbumHelperForWidget getAlbumHelper(Context context) {
		PicasaApplication application = getApplication(context);
		return new AlbumHelperForWidget(application);
	}

	protected HttpTransport getTransport(Context context) {
		PicasaApplication application = (PicasaApplication) context
				.getApplicationContext();
		return application.getTransport();
	}

	protected AccountHelperForWidget getAccountHelper(Context context) {
		PicasaApplication application = getApplication(context);
		return new AccountHelperForWidget(application);
	}

	public void onDestroy() {
		Log.d(TAG, "onDestroy " + this);
		savePositionToSharedPreferences(
				getApplication(getApplicationContext()), -1);
		super.onDestroy();

	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.d(TAG, "onStart " + this);
		String command = intent.getAction();
		Log.d(TAG, "command " + command);
		int position = intent.getIntExtra("position", 0);
		if (command.equals(UPDATE)) {
			position = getPositionFromSharedPreferences(getApplication(getApplicationContext())) + 1;
		}
		int widgetId = intent.getExtras().getInt(
				AppWidgetManager.EXTRA_APPWIDGET_ID);
		boolean toggleButton = false;
		boolean getNewImage = true;
		if (command.startsWith(SHOW_BUTTON)) {
			toggleButton = Boolean.valueOf(command.split("=")[1]);
			getNewImage = false;
		} else {
			toggleButton = false;
			getNewImage = true;
		}

		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(getApplicationContext());
		try {
			showAlbumImage(getApplicationContext(), appWidgetManager, widgetId,
					position, getNewImage, toggleButton);
		} catch (Exception e) {
			e.printStackTrace();
			showSelectAlbumView(getApplicationContext(), appWidgetManager,
					widgetId, e.getMessage());
		}

		super.onStart(intent, startId);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
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

	private List<PhotoEntry> getPhotos(Context context) throws Exception {
		PicasaApplication application = getApplication(context);
		if (application.getPhotos().size() == 0) {
			AlbumFeed albumFeed = getAlbum(context);
			Log.d(TAG, "albumFeed " + albumFeed);
			application.addPhotos(albumFeed.photos);

		}
		return application.getPhotos();
	}

	protected void savePositionToSharedPreferences(
			PicasaApplication application, int position) {
		SharedPreferences settings = application.getSharedPreferences();
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("position", position);
		editor.commit();
	}

	protected int getPositionFromSharedPreferences(PicasaApplication application) {
		SharedPreferences settings = application.getSharedPreferences();
		return settings.getInt("position", -1);
	}

	protected SharedPreferences getSharedPreferences(
			PicasaApplication application) {
		return application.getSharedPreferences();
	}

	protected void showAlbumImage(Context context,
			final AppWidgetManager appWidgetManager, final int widgetId,
			int position, boolean getNewImage, boolean toggleButton)
			throws Exception {

		Log.d(TAG, "position: " + position + " widgetId: " + widgetId);
		List<PhotoEntry> photos = getPhotos(context);

		int back = position - 1;
		int forward = position + 1;
		final RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget_image_layout);
		views.setOnClickPendingIntent(R.id.backward, MyWidgetProvider
				.makeWidgetServicePendingIntent(getApplicationContext(),
						BACKWARD, widgetId, back));
		views.setOnClickPendingIntent(R.id.forward, MyWidgetProvider
				.makeWidgetServicePendingIntent(getApplicationContext(),
						FORWARD, widgetId, forward));
		views.setOnClickPendingIntent(R.id.imageView, MyWidgetProvider
				.makeWidgetServicePendingIntent(getApplicationContext(),
						SHOW_BUTTON + "=" + !toggleButton, widgetId, position));

		views.setViewVisibility(R.id.backward,
				(back >= 0 & toggleButton) ? View.VISIBLE : View.GONE);

		views.setViewVisibility(R.id.forward,
				(forward < photos.size() & toggleButton) ? View.VISIBLE
						: View.GONE);

		int positionToMoveTo = position;
		// reset position back to zero
		if (position >= photos.size()) {
			position = 0;
			positionToMoveTo = -1;
		}

		// AsyncImageLoader loader = new AsyncImageLoader();

		PhotoEntry photo = photos.get(position);
		savePositionToSharedPreferences(getApplication(context),
				positionToMoveTo);
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

		String fileName = "myImage" + position + ".jpg";
		if (getNewImage) {
			Bitmap image = getImageBitmap(url);
			Log.d(TAG, "bitmap loaded " + image);
			boolean deletedFlag = context.deleteFile(fileName);
			Log.d(TAG, "deleted file flag " + deletedFlag);
			FileOutputStream fileOutputStream = context.openFileOutput(
					fileName, Context.MODE_WORLD_READABLE);
			BufferedOutputStream bos = new BufferedOutputStream(
					fileOutputStream);
			image.compress(CompressFormat.JPEG, 80, bos);
			bos.flush();
			bos.close();
		}

		views.setImageViewUri(R.id.imageView,
				Uri.fromFile(context.getFileStreamPath(fileName)));
		// views.setImageViewBitmap(R.id.imageView, image);
		// switch (position) {
		// case 0:
		// views.setImageViewResource(R.id.imageView,
		// R.drawable.sample_thumb_0);
		// break;
		// case 1:
		// views.setImageViewResource(R.id.imageView,
		// R.drawable.sample_thumb_1);
		// break;
		// case 2:
		// views.setImageViewResource(R.id.imageView,
		// R.drawable.sample_thumb_2);
		// break;
		// default:
		// break;
		// }
		appWidgetManager.updateAppWidget(widgetId, views);
		// appWidgetManager.updateAppWidget(new ComponentName(
		// getApplicationContext(), MyWidgetProvider.class), views);
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
}
