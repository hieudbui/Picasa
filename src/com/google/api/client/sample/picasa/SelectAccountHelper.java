package com.google.api.client.sample.picasa;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;

public class SelectAccountHelper {
	private static final int DIALOG_ACCOUNTS = 0;

	private PicasaAndroidSample selectAlbumActivity;

	public SelectAccountHelper(PicasaAndroidSample selectAccountActivity) {
		this.selectAlbumActivity = selectAccountActivity;
	}

	public void showDialog() {
		selectAlbumActivity.showDialog(DIALOG_ACCOUNTS);
	}

	public Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_ACCOUNTS:
			AlertDialog.Builder builder = new AlertDialog.Builder(
					selectAlbumActivity);
			builder.setTitle("Select a Google account");
			final String[] names = selectAlbumActivity.getAccountHelper()
					.getAccountNames();
			builder.setItems(names, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					selectAlbumActivity.getAccountHelper().gotAccount(
							names[which]);
				}

			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

				public void onCancel(DialogInterface dialog) {
					SelectAccountHelper.this.selectAlbumActivity.finish();
				}
			});
			return builder.create();
		}
		return null;
	}
}
