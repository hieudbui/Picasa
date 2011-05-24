package com.google.api.client.sample.picasa;

import android.content.SharedPreferences;

public class AlbumHelperForWidget extends AlbumHelper {

	PicasaApplication application;

	public AlbumHelperForWidget(PicasaApplication application) {
		super(null);
		this.application = application;
	}

	protected SharedPreferences getSharedPreferences() {
		return application.getSharedPreferences();
	}
}
