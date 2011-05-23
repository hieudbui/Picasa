package com.google.api.client.sample.picasa;

import java.io.IOException;
import java.util.Date;

import android.content.SharedPreferences;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.google.api.client.sample.picasa.model.AlbumEntry;
import com.google.api.client.util.DateTime;

public class ContextMenuHelper {

	private static final int CONTEXT_EDIT = 0;

	private static final int CONTEXT_DELETE = 1;

	private static final int CONTEXT_LOGGING = 2;

	private PicasaAndroidSample selectAlbumActivity;

	public ContextMenuHelper(PicasaAndroidSample selectAccountActivity) {
		this.selectAlbumActivity = selectAccountActivity;
	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.add(0, CONTEXT_EDIT, 0, "Update Title");
		menu.add(0, CONTEXT_DELETE, 0, "Delete");
		SharedPreferences settings = selectAlbumActivity.getSharedPreferences();
		boolean logging = settings.getBoolean("logging", false);
		menu.add(0, CONTEXT_LOGGING, 0, "Logging").setCheckable(true)
				.setChecked(logging);
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		AlbumEntry album = selectAlbumActivity.getAlbumHelper().getAlbums()
				.get((int) info.id);
		try {
			switch (item.getItemId()) {
			case CONTEXT_EDIT:
				AlbumEntry patchedAlbum = album.clone();
				patchedAlbum.title = album.title + " UPDATED "
						+ new DateTime(new Date());
				patchedAlbum.executePatchRelativeToOriginal(
						selectAlbumActivity.getTransport(), album);
				selectAlbumActivity.getAlbumHelper().executeRefreshAlbums();
				return true;
			case CONTEXT_DELETE:
				album.executeDelete(selectAlbumActivity.getTransport());
				selectAlbumActivity.getAlbumHelper().executeRefreshAlbums();
				return true;
			case CONTEXT_LOGGING:
				SharedPreferences settings = selectAlbumActivity
						.getSharedPreferences();
				boolean logging = settings.getBoolean("logging", false);
				selectAlbumActivity.setLogging(!logging);
				return true;
			default:
				return selectAlbumActivity.onContextItemSelected(item);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}
