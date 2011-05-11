/*
 * Copyright (c) 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.sample.picasa.model;

import com.google.api.client.util.Key;

import java.io.Serializable;
import java.util.List;

/**
 * @author Yaniv Inbar
 */
public class Link implements Serializable
{
    private static final long serialVersionUID = 1897314392117294219L;

    @Key("@href")
    public String href;

    @Key("@rel")
    public String rel;

    public static String find(List<Link> links, String rel)
    {
        if (links != null)
        {
            for (Link link : links)
            {
                if (rel.equals(link.rel))
                {
                    return link.href;
                }
            }
        }
        return null;
    }

    public static final class Rel
    {
        public static final String FEED = "http://schemas.google.com/g/2005#feed";
        public static final String ENTRY_EDIT = "edit";
    }
}
