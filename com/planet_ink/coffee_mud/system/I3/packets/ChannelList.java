package com.planet_ink.coffee_mud.system.I3.packets;

import java.util.Hashtable;

public class ChannelList {
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
        else {
            return (Channel)list.get(channel);
        }
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
