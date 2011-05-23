package com.google.api.client.sample.picasa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.sample.picasa.model.AlbumEntry;
import com.google.api.client.sample.picasa.model.PicasaUrl;
import com.google.api.client.sample.picasa.model.UserFeed;
import com.google.api.client.util.DateTime;

public class AlbumHelper {

	private static final String TAG = "PicasaAndroidSample.AlbumHelper";

	private final List<AlbumEntry> albums = new ArrayList<AlbumEntry>();

	private String postLink;

	private PicasaAndroidSample selectAlbumActivity;

	public AlbumHelper(PicasaAndroidSample selectAccountActivity) {
		this.selectAlbumActivity = selectAccountActivity;
	}

	@SuppressWarnings("unused")
	private void executeRefreshPhotos(AlbumEntry album) {
		String[] photoNames;
		PicasaUrl url = PicasaUrl.relativeToRoot("feed/api/user/default");

	}

	public List<AlbumEntry> getAlbums() {
		return this.albums;
	}

	public void createNewAlbum() {
		AlbumEntry album = new AlbumEntry();
		album.access = "private";
		album.title = "Album " + new DateTime(new Date());
		try {
			AlbumEntry.executeInsert(selectAlbumActivity.getTransport(), album,
					this.postLink);
		} catch (IOException e) {
			e.printStackTrace();
		}
		executeRefreshAlbums();
	}

	public void executeRefreshAlbums() {
		String[] albumNames;
		List<AlbumEntry> albums = getAlbums();
		albums.clear();
		try {
			PicasaUrl url = PicasaUrl.relativeToRoot("feed/api/user/default");
			// page through results
			while (true) {
				UserFeed userFeed = UserFeed.executeGet(
						selectAlbumActivity.getTransport(), url);
				this.postLink = userFeed.getPostLink();
				if (userFeed.albums != null) {
					albums.addAll(userFeed.albums);
				}
				String nextLink = userFeed.getNextLink();
				if (nextLink == null) {
					break;
				}
			}
			int numAlbums = albums.size();
			albumNames = new String[numAlbums];
			for (int i = 0; i < numAlbums; i++) {
				albumNames[i] = albums.get(i).title;
			}
		} catch (IOException e) {
			handleException(e);
			albumNames = new String[] { e.getMessage() };
			albums.clear();
		}
		selectAlbumActivity.setListAdapter(new ArrayAdapter<String>(
				selectAlbumActivity, android.R.layout.simple_list_item_1,
				albumNames));
	}

	public void handleException(Exception e) {
		e.printStackTrace();
		SharedPreferences settings = selectAlbumActivity.getSharedPreferences();
		boolean log = settings.getBoolean("logging", false);
		if (e instanceof HttpResponseException) {
			HttpResponse response = ((HttpResponseException) e).response;
			int statusCode = response.statusCode;
			try {
				response.ignore();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			if (statusCode == 401 || statusCode == 403) {
				selectAlbumActivity.getAccountHelper().gotAccount(true);
				return;
			}
			if (log) {
				try {
					Log.e(TAG, response.parseAsString());
				} catch (IOException parseException) {
					parseException.printStackTrace();
				}
			}
		}
		if (log) {
			Log.e(TAG, e.getMessage(), e);
		}
	}
}
