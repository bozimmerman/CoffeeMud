package com.planet_ink.coffee_mud.i3.packets;
import com.planet_ink.coffee_mud.i3.server.Server;


import java.util.Vector;

public class ChannelDelete extends Packet  {
    public String channel = null;

	public ChannelDelete()
	{
		super();
        type = ChannelPacket.CHAN_REMOVE;
	}
    public ChannelDelete(Vector v) throws InvalidPacketException {
        super(v);
        try {
			type = ChannelPacket.CHAN_REMOVE;
			channel = (String)v.elementAt(6);
            channel = Intermud.getLocalChannel(channel);
        }
        catch( ClassCastException e ) {
            throw new InvalidPacketException();
        }
    }

	
    public ChannelDelete(int t, String chan, String who) {
        super();
        type = t;
        channel = chan;
        sender_name = who;
    }

    public void send() throws InvalidPacketException {
        if( channel == null ) {
            throw new InvalidPacketException();
        }
        channel = Intermud.getRemoteChannel(channel);
        super.send();
    }

    public String toString() {
        NameServer n = Intermud.getNameServer();
		String str=
			 "({\"channel-remove\",5,\"" + Server.getMudName() + "\",\"" +
               sender_name + "\",\""+n.name+"\",0,\"" + channel + "\",})";
		return str;
    }
}
