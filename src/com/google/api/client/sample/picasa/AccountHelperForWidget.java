package com.google.api.client.sample.picasa;

import android.content.Context;
import android.content.SharedPreferences;

public class AccountHelperForWidget extends AccountHelper {

	PicasaApplication application;

	public AccountHelperForWidget(PicasaApplication application) {
		super(null);
		this.application = application;
	}

	protected SharedPreferences getSharedPreferences() {
		return application.getSharedPreferences();
	}

	public Context getContext() {
		return application;
	}

}
