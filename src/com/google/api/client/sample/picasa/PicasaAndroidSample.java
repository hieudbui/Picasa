/*
 * Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.sample.picasa;

import android.accounts.Account;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.sample.picasa.model.AlbumEntry;

/**
 * Sample for Picasa Web Albums Data API using the Atom wire format. It shows
 * how to authenticate, get albums, add a new album, update it, and delete it.
 * <p>
 * It also demonstrates how to upload a photo to the "Drop Box" album. To see
 * this example in action, take a picture, click "Share", and select
 * "Picasa Basic Android Sample".
 * </p>
 * <p>
 * To enable logging of HTTP requests/responses, run this command: {@code adb
 * shell setprop log.tag.HttpTransport DEBUG}. Then press-and-hold an album, and
 * enable "Logging".
 * </p>
 * 
 */
public final class PicasaAndroidSample extends ListActivity {

	private static final String TAG = "PicasaAndroidSample";

	private OptionsMenuHelper optionsMenuHelper = new OptionsMenuHelper(this);

	private AccountHelper accountHelper = new AccountHelper(this);

	private SelectAccountHelper selectAccountHelper = new SelectAccountHelper(
			this);

	private ContextMenuHelper contextMenuHelper = new ContextMenuHelper(this);

	private AlbumHelper albumHelper = new AlbumHelper(this);

	public PicasaAndroidSample() {
	}

	public AccountHelper getAccountHelper() {
		return accountHelper;
	}

	public SelectAccountHelper getSelectAccountHelper() {
		return selectAccountHelper;
	}

	public OptionsMenuHelper getOptionsMenuHelper() {
		return optionsMenuHelper;
	}

	public AlbumHelper getAlbumHelper() {
		return albumHelper;
	}

	public HttpTransport getTransport() {
		return ((PicasaApplication) this.getApplication()).getTransport();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "on create called " + this);

		super.onCreate(savedInstanceState);
		SharedPreferences settings = this.getSharedPreferences();
		setLogging(settings.getBoolean("logging", true));
		getListView().setTextFilterEnabled(true);
		registerForContextMenu(getListView());
		selectAccountHelper.showDialog();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		return selectAccountHelper.onCreateDialog(id);
	}

	public SharedPreferences getSharedPreferences() {
		return ((PicasaApplication) this.getApplication())
				.getSharedPreferences();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		accountHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		contextMenuHelper.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Log.d(TAG, "position=" + position + " id=" + id);
		AlbumEntry album = albumHelper.getAlbums().get(position);
		albumHelper.saveAlbumToSharedPreferences(album);
		String feedLink = album.getFeedLink();
		Intent intent = new Intent(this, PhotosGrid.class);
		intent.putExtra("albumLink", feedLink);
		startActivity(intent);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return contextMenuHelper.onContextItemSelected(item);
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

	public void setLogging(boolean logging) {
		SharedPreferences settings = this.getSharedPreferences();
		boolean currentSetting = settings.getBoolean("logging", false);
		if (currentSetting != logging) {
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("logging", logging);
			editor.commit();
		}
	}
}
