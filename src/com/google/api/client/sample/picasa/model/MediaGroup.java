package com.google.api.client.sample.picasa.model;

import java.io.Serializable;
import java.util.List;

import com.google.api.client.util.Key;

public class MediaGroup implements Serializable
{
    private static final long serialVersionUID = 7909347424397969975L;

    @Key("media:content")
    public MediaContent mediaContent;

    @Key("media:thumbnail")
    public List<MediaContent> thumbnails;

    @Key("media:description")
    public String description;

    public MediaContent getSmallThumbNail()
    {
        return getThumbNail(0);
    }

    public MediaContent getMediumThumbNail()
    {
        return getThumbNail(1);
    }

    public MediaContent getLargeThumbNail()
    {
        return getThumbNail(2);
    }

    private MediaContent getThumbNail(int size)
    {
        if (thumbnails.size() == 3)
        {
            return thumbnails.get(size);
        }
        else if (thumbnails.size() == 2)
        {
            return thumbnails.get(size > 2 ? size-- : size);
        }
        else if (thumbnails.size() == 1)
        {
            return thumbnails.get(0);
        }
        return null;
    }
}
