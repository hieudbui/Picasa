package com.google.api.client.sample.picasa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.sample.picasa.AsyncImageLoader.ImageCallback;
import com.google.api.client.sample.picasa.model.AlbumFeed;
import com.google.api.client.sample.picasa.model.PhotoEntry;
import com.google.api.client.sample.picasa.model.PicasaUrl;

/**
 * A grid that displays a set of framed photos.
 * 
 */
public class PhotosGrid extends Activity {
	private static final String TAG = "PhotosGrid";

	private static final int DIALOG_PROGRESS = 1;

	private String albumLink;

	private GridView gridView;
	final Handler getPhotos = new Handler();

	final Runnable updateGrid = new Runnable() {
		public void run() {
			updateGridInUI();
			dismissDialog(DIALOG_PROGRESS);
		}
	};

	private ClientConnectionManager connManager;
	private HttpClient httpClient;

	public PhotosGrid() {
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params,
				HTTP.DEFAULT_CONTENT_CHARSET);

		HttpProtocolParams.setUseExpectContinue(params, true);

		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		registry.register(new Scheme("https", SSLSocketFactory
				.getSocketFactory(), 443));
		connManager = new ThreadSafeClientConnManager(params, registry);
		httpClient = new DefaultHttpClient(connManager, params);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "on create called " + this);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.photos_grid);
		gridView = (GridView) findViewById(R.id.myGrid);

		Intent intent = getIntent();
		albumLink = intent.getStringExtra("albumLink");

		showDialog(DIALOG_PROGRESS);

		new Thread() {
			@Override
			public void run() {
				try {
					executeRefreshPhotos();
					getPhotos.post(updateGrid);
				} catch (Exception e) {
					handleException(e);
				}
			}
		}.start();
	}

	public void onStart() {
		super.onStart();
		Log.d(TAG, "on start called");
	}

	public void onRestart() {
		super.onRestart();
		Log.d(TAG, "on restart called");
	}

	public void onResume() {
		super.onResume();
		Log.d(TAG, "on resumes called");
	}

	public void onPause() {
		super.onResume();
		Log.d(TAG, "on pause called");
	}

	private void handleException(Exception e) {
		// TODO
		// handle exception
		e.printStackTrace();
	}

	public HttpTransport getTransport() {
		return ((PicasaApplication) this.getApplication()).getTransport();
	}

	private void updateGridInUI() {
		ListAdapter adapter;
		if (this.getPhotos() != null && this.getPhotos().size() > 0) {
			adapter = new ImageAdapter(this, this.getPhotos(), gridView);
		} else {
			adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1,
					new String[] { "no photos found" });
		}
		// ImageAdapter2 adapter = new ImageAdapter2(this);
		gridView.setAdapter(adapter);
	}

	private void executeRefreshPhotos() throws IOException {
		AlbumFeed albumFeed = AlbumFeed.executeGet(getTransport(),
				new PicasaUrl(albumLink));
		this.setPhotos(albumFeed.photos);
	}

	private List<PhotoEntry> getPhotos() {
		return ((PicasaApplication) this.getApplication()).getPhotos();
	}

	public void setPhotos(List<PhotoEntry> photos) {
		((PicasaApplication) this.getApplication()).setPhotos(photos);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog progressDialog;
		switch (id) {
		case DIALOG_PROGRESS:
			progressDialog = new ProgressDialog(PhotosGrid.this);
			progressDialog.setTitle(R.string.loading);
			((ProgressDialog) progressDialog)
					.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			break;
		default:
			progressDialog = null;

		}
		return progressDialog;
	}

	public class ImageAdapter extends BaseAdapter {
		private Context mContext;
		private List<PhotoEntry> photos;
		private AsyncImageLoader asyncImageLoader;
		private GridView gridView;

		public ImageAdapter(Context c, List<PhotoEntry> photos,
				GridView gridView) {
			mContext = c;
			this.photos = photos != null ? photos : new ArrayList<PhotoEntry>();
			this.gridView = gridView;
			asyncImageLoader = new AsyncImageLoader(httpClient);
		}

		public int getCount() {
			return photos.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			Log.d(TAG, "position: " + position + " view: " + convertView);
			ImageView imageView;
			if (convertView == null) {
				imageView = new ImageView(mContext);
				imageView.setLayoutParams(new GridView.LayoutParams(160, 160));
				imageView.setAdjustViewBounds(true);
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setPadding(5, 5, 5, 5);
			} else {
				imageView = (ImageView) convertView;
			}

			PhotoEntry photoEntry = photos.get(position);
			String url = photoEntry.mediaGroup.getLargeThumbNail().url;
			imageView.setTag(url);
			imageView.setTag(R.id.position, position);
			imageView.setBackgroundColor(Color.RED);
			Drawable cachedImage = asyncImageLoader.loadDrawable(url,
					new ImageCallback() {
						public void imageLoaded(Drawable imageDrawable,
								String imageUrl) {
							ImageView imageViewByTag = (ImageView) gridView
									.findViewWithTag(imageUrl);
							if (imageViewByTag != null) {
								imageViewByTag.setImageDrawable(imageDrawable);
								imageViewByTag
										.setOnClickListener(new View.OnClickListener() {
											public void onClick(View view) {
												Log.d(TAG,
														"view id="
																+ view.getId());
												Integer position = (Integer) view
														.getTag(R.id.position);
												Intent intent = new Intent(
														PhotosGrid.this,
														PhotoView.class);
												intent.putExtra("position",
														position);
												startActivity(intent);
											}
										});
							}
						}
					});

			imageView.setImageDrawable(cachedImage);
			return imageView;
		}
	}
}
