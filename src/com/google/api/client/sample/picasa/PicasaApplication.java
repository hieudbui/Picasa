package com.google.api.client.sample.picasa;

import java.util.List;

import android.app.Application;

import com.google.api.client.sample.picasa.model.PhotoEntry;

public class PicasaApplication extends Application
{

    private List<PhotoEntry> photos;

    @Override
    public void onCreate()
    {
    }

    @Override
    public void onTerminate()
    {
    }

    public void setPhotos(List<PhotoEntry> photos)
    {
        this.photos = photos;
    }

    public List<PhotoEntry> getPhotos()
    {
        return this.photos;
    }

}
