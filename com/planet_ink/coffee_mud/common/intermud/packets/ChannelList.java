package com.planet_ink.coffee_mud.i3.packets;

import java.util.Hashtable;
import java.io.Serializable;

/**
 * Copyright (c) 1996 George Reese
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
public class ChannelList implements Serializable {
	public static final long serialVersionUID=0;
    private int id;
    private Hashtable list;

    public ChannelList() {
        super();
        id = -1;
        list = new Hashtable(10, 5);
    }

    public ChannelList(int i) {
        this();
        id = i;
    }

    public void addChannel(Channel c ) {
        if( c.channel == null ) {
            return;
        }
        list.put(c.channel, c);
    }

    public Channel getChannel(String channel) {
        if( !list.containsKey(channel) ) {
            return null;
        }
        return (Channel)list.get(channel);
    }

    public void removeChannel(Channel c) {
        if( c.channel == null ) {
            return;
        }
        list.remove(c.channel);
    }

    public int getChannelListId() {
        return id;
    }

    public void setChannelListId(int x) {
        id = x;
    }

    public Hashtable getChannels() {
        return list;
    }
}
