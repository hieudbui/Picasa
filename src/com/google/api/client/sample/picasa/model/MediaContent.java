package com.google.api.client.sample.picasa.model;

import java.io.Serializable;

import com.google.api.client.util.Key;

public class MediaContent implements Serializable
{
    private static final long serialVersionUID = -2526216582983974263L;

    @Key("@url")
    public String url;

    @Key("@height")
    public int height;

    @Key("@width")
    public int width;

}
