package com.planet_ink.coffee_mud.system.I3.packets;
import com.planet_ink.coffee_mud.system.I3.server.Server;


import java.util.Vector;

public class ChannelListen extends Packet  {
    public String channel = null;
	public String onoff="0";
	
	public ChannelListen()
	{
		super();
        type = ChannelPacket.CHAN_LISTEN;
	}
    public ChannelListen(Vector v) throws InvalidPacketException {
        super(v);
        try {
			type = ChannelPacket.CHAN_LISTEN;
			channel = (String)v.elementAt(6);
			onoff = (String)v.elementAt(7);
        }
        catch( ClassCastException e ) {
            throw new InvalidPacketException();
        }
    }

	
    public ChannelListen(int t, String chan, String setonoff) {
        super();
        type = t;
        channel = chan;
		onoff=setonoff;
    }

    public void send() throws InvalidPacketException {
        if( channel == null  ) {
            throw new InvalidPacketException();
        }
        super.send();
    }

    public String toString() {
		String str=
			 "({\"channel-listen\",5,\"" + Server.getMudName() + "\",0,0,0,\"" + channel + "\"," +
               onoff + ",})";
		return str;
    }
}
