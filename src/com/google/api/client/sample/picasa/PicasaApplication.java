package com.google.api.client.sample.picasa;

import java.util.List;

import android.app.Application;
import android.content.SharedPreferences;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.sample.picasa.model.PhotoEntry;
import com.google.api.client.sample.picasa.model.Util;
import com.google.api.client.xml.atom.AtomParser;

public class PicasaApplication extends Application {

	private static final String PREF = "MyPrefs";

	private HttpTransport transport;

	private List<PhotoEntry> photos;

	@Override
	public void onCreate() {
		transport = GoogleTransport.create();
		GoogleHeaders headers = (GoogleHeaders) transport.defaultHeaders;
		headers.setApplicationName("Google-PicasaAndroidAample/1.0");
		headers.gdataVersion = "2";
		AtomParser parser = new AtomParser();
		parser.namespaceDictionary = Util.NAMESPACE_DICTIONARY;
		transport.addParser(parser);
	}

	@Override
	public void onTerminate() {
	}

	public void setPhotos(List<PhotoEntry> photos) {
		this.photos = photos;
	}

	public List<PhotoEntry> getPhotos() {
		return this.photos;
	}

	public HttpTransport getTransport() {
		return transport;
	}

	public SharedPreferences getSharedPreferences() {
		return this.getSharedPreferences(PREF, 0);
	}

}
