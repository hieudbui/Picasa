package com.google.api.client.sample.picasa;

import android.view.Menu;
import android.view.MenuItem;

public class OptionsMenuHelper {
	private static final int MENU_ADD = 0;

	private static final int MENU_ACCOUNTS = 1;

	private PicasaAndroidSample selectAlbumActivity;

	public OptionsMenuHelper(PicasaAndroidSample selectAccountActivity) {
		this.selectAlbumActivity = selectAccountActivity;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_ADD, 0, "New album");
		menu.add(0, MENU_ACCOUNTS, 0, "Switch Account");
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ADD:
			selectAlbumActivity.getAlbumHelper().createNewAlbum();
			return true;
		case MENU_ACCOUNTS:
			selectAlbumActivity.getSelectAccountHelper().showDialog();
			return true;
		}
		return false;
	}

}
