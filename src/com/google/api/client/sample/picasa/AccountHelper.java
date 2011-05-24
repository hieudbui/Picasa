package com.google.api.client.sample.picasa;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.api.client.googleapis.GoogleHeaders;

public class AccountHelper {

	private static final String TAG = "PicasaAndroidSample.AccountHelper";

	private static final String AUTH_TOKEN_TYPE = "lh2";

	private static final int REQUEST_AUTHENTICATE = 0;

	private PicasaAndroidSample selectAlbumActivity;

	public AccountHelper(PicasaAndroidSample selectAccountActivity) {
		this.selectAlbumActivity = selectAccountActivity;
	}

	public Account[] getAccounts() {
		AccountManager manager = AccountManager.get(getContext());
		return manager.getAccountsByType("com.google");
	}

	public String[] getAccountNames() {
		Account[] accounts = getAccounts();
		int size = accounts.length;
		String[] names = new String[size];
		for (int i = 0; i < size; i++) {
			names[i] = accounts[i].name;
		}
		return names;
	}

	public void gotAccount(String accountName) {
		Account[] accounts = getAccounts();
		int size = accounts.length;
		for (int i = 0; i < size; i++) {
			Account account = accounts[i];
			if (account.name.equals(accountName)) {
				gotAccount(account);
				break;
			}
		}
	}

	public void gotAccount(final Account account) {
		saveAccountToSharedPreferences(account);
		new Thread() {

			@Override
			public void run() {
				try {
					AccountManager manager = AccountManager.get(getContext());
					final Bundle bundle = manager.getAuthToken(account,
							AUTH_TOKEN_TYPE, true, null, null).getResult();
					selectAlbumActivity.runOnUiThread(new Runnable() {

						public void run() {
							try {
								if (bundle
										.containsKey(AccountManager.KEY_INTENT)) {
									Intent intent = bundle
											.getParcelable(AccountManager.KEY_INTENT);
									int flags = intent.getFlags();
									flags &= ~Intent.FLAG_ACTIVITY_NEW_TASK;
									intent.setFlags(flags);
									selectAlbumActivity.startActivityForResult(
											intent, REQUEST_AUTHENTICATE);
								} else if (bundle
										.containsKey(AccountManager.KEY_AUTHTOKEN)) {
									authenticatedClientLogin(bundle
											.getString(AccountManager.KEY_AUTHTOKEN));
								}
							} catch (Exception e) {
								handleException(e);
							}
						}
					});
				} catch (Exception e) {
					handleException(e);
				}
			}
		}.start();
	}

	protected void saveAccountToSharedPreferences(final Account account) {
		SharedPreferences settings = getSharedPreferences();
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("accountName", account.name);
		editor.commit();
	}

	protected String getAccountNameFromSharedPreferences() {
		SharedPreferences settings = getSharedPreferences();
		String accountName = settings.getString("accountName", null);
		return accountName;
	}

	protected SharedPreferences getSharedPreferences() {
		return selectAlbumActivity.getSharedPreferences();
	}

	public Context getContext() {
		return selectAlbumActivity;
	}

	public Account getAccountFromSharedPreferences() {
		String accountName = getAccountNameFromSharedPreferences();
		if (accountName != null) {
			Account[] accounts = getAccounts();
			int size = accounts.length;
			for (int i = 0; i < size; i++) {
				Account account = accounts[i];
				if (accountName.equals(account.name)) {
					return account;
				}
			}
		}
		return null;
	}

	public void gotAccount(boolean tokenExpired) {
		AccountManager manager = AccountManager.get(getContext());
		Account account = getAccountFromSharedPreferences();
		String authToken = getAuthTokenFromSharedPreferences();
		if (account != null) {
			if (tokenExpired) {
				manager.invalidateAuthToken("com.google", authToken);
			}
			gotAccount(account);
		}
		// selectAlbumActivity.getSelectAccountHelper().showDialog();
	}

	private void handleException(Exception e) {
		e.printStackTrace();
		SharedPreferences settings = getSharedPreferences();
		boolean log = settings.getBoolean("logging", false);
		if (log) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	protected void saveAuthTokenToSharedPreferences(String authToken) {
		SharedPreferences settings = getSharedPreferences();
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("authToken", authToken);
		editor.commit();
	}

	protected String getAuthTokenFromSharedPreferences() {
		SharedPreferences settings = getSharedPreferences();
		String accountName = settings.getString("authToken", null);
		return accountName;
	}

	private void authenticatedClientLogin(String authToken) {
		saveAuthTokenToSharedPreferences(authToken);
		((GoogleHeaders) selectAlbumActivity.getTransport().defaultHeaders)
				.setGoogleLogin(authToken);
		selectAlbumActivity.getAlbumHelper().executeRefreshAlbums();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_AUTHENTICATE:
			if (resultCode == PicasaAndroidSample.RESULT_OK) {
				gotAccount(false);
			} else {
				selectAlbumActivity.getSelectAccountHelper().showDialog();
			}
			break;
		}
	}

}
