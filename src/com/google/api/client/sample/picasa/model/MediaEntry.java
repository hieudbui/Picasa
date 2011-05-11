package com.google.api.client.sample.picasa.model;

import java.io.Serializable;

import com.google.api.client.util.Key;

public class MediaEntry extends Entry implements Serializable
{
    private static final long serialVersionUID = 7797489559391257111L;
    @Key("media:group")
    public MediaGroup mediaGroup;
}
