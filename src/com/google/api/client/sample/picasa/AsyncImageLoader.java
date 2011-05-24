package com.google.api.client.sample.picasa;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;

public class AsyncImageLoader {
	private HashMap<String, SoftReference<Drawable>> imageCache;

	private ClientConnectionManager connManager;
	private HttpClient httpClient;

	public AsyncImageLoader() {
		instantiateHttpClient();
		imageCache = new HashMap<String, SoftReference<Drawable>>();
	}

	protected void instantiateHttpClient() {
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
		this.httpClient = new DefaultHttpClient(connManager, params);
	}

	private Drawable loadImageFromCache(String imageUrl) {
		if (imageCache.containsKey(imageUrl)) {
			SoftReference<Drawable> softReference = imageCache.get(imageUrl);
			Drawable drawable = softReference.get();
			if (drawable != null) {
				return drawable;
			}
		}
		return null;
	}

	public Drawable loadDrawable(String imageUrl) {
		Drawable drawable = loadImageFromCache(imageUrl);
		if (drawable != null) {
			return drawable;
		}
		return loadImageFromUrl(imageUrl, httpClient);
	}

	public Drawable loadDrawable(final String imageUrl,
			final ImageCallback imageCallback) {
		Drawable drawable = loadImageFromCache(imageUrl);
		if (drawable != null) {
			return drawable;
		}
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				imageCallback.imageLoaded((Drawable) message.obj, imageUrl);
			}
		};
		new Thread() {
			@Override
			public void run() {
				Drawable drawable = loadImageFromUrl(imageUrl, httpClient);
				Message message = handler.obtainMessage(0, drawable);
				handler.sendMessage(message);
			}
		}.start();
		return null;
	}

	public Drawable loadImageFromUrl(String url, HttpClient httpClient) {
		InputStream inputStream;
		try {
			HttpGet request = new HttpGet(url);
			HttpResponse response = httpClient.execute(request);
			inputStream = response.getEntity().getContent();
			// inputStream = new URL(url).openStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Drawable drawable = Drawable.createFromStream(inputStream, "src");
		imageCache.put(url, new SoftReference<Drawable>(drawable));
		return drawable;
	}

	public interface ImageCallback {
		public void imageLoaded(Drawable imageDrawable, String imageUrl);
	}
}
