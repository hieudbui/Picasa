package com.google.api.client.sample.picasa.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

import android.graphics.Bitmap;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Key;

public class PhotoEntry extends MediaEntry implements Serializable
{
    private static final long serialVersionUID = -3728636949543404765L;

    @Key("gphoto:access")
    public String access;

    @Key
    public Category category = Category.newKind("photo");

    public Map<String, Bitmap> bitMaps = new Hashtable<String, Bitmap>();

    public Bitmap getBitmap(String url)
    {
        return bitMaps.get(url);
    }

    public void addBitmap(String url, Bitmap bm)
    {
        bitMaps.put(url, bm);
    }

    @Override
    public PhotoEntry clone()
    {
        return (PhotoEntry) super.clone();
    }

    public PhotoEntry executePatchRelativeToOriginal(HttpTransport transport, PhotoEntry original)
            throws IOException
    {
        return (PhotoEntry) super.executePatchRelativeToOriginal(transport, original);
    }

    public static PhotoEntry executeInsert(HttpTransport transport, PhotoEntry entry,
            String postLink) throws IOException
    {
        return (PhotoEntry) Entry.executeInsert(transport, entry, postLink);
    }
}
