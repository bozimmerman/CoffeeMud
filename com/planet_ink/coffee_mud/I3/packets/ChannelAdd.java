package com.planet_ink.coffee_mud.system.I3.packets;
import com.planet_ink.coffee_mud.system.I3.server.Server;


import java.util.Vector;

public class ChannelAdd extends Packet  {
    public String channel = null;

	public ChannelAdd()
	{
		super();
        type = ChannelPacket.CHAN_ADD;
	}
    public ChannelAdd(Vector v) throws InvalidPacketException {
        super(v);
        try {
			type = ChannelPacket.CHAN_ADD;
			channel = (String)v.elementAt(6);
        }
        catch( ClassCastException e ) {
            throw new InvalidPacketException();
        }
    }

	
    public ChannelAdd(int t, String chan, String who) {
        super();
        type = t;
        channel = chan;
        sender_name = who;
    }

    public void send() throws InvalidPacketException {
        if( channel == null  ) {
            throw new InvalidPacketException();
        }
        super.send();
    }

    public String toString() {
        NameServer n = Intermud.getNameServer();
		String str=
			 "({\"channel-add\",5,\"" + Server.getMudName() + "\",\"" +
               sender_name + "\",\""+n.name+"\",0,\"" + channel + "\",0,})";
		return str;
    }
}
