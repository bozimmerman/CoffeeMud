package com.planet_ink.coffee_mud.system.I3.packets;

import com.planet_ink.coffee_mud.system.I3.server.Server;
import java.util.Vector;

public class ChannelWhoReply extends Packet {
    public String channel = null;
	public Vector who=null;

	public ChannelWhoReply()
	{
		super();
        type = Packet.CHAN_WHO_REP;
	}
	
    public ChannelWhoReply(Vector v) throws InvalidPacketException {
        super(v);
        try {
            type = Packet.CHAN_WHO_REP;
			channel = (String)v.elementAt(6);
            channel = Intermud.getLocalChannel(channel);
			who = (Vector)v.elementAt(7);
        }
        catch( ClassCastException e ) {
            throw new InvalidPacketException();
        }
    }

    public void send() throws InvalidPacketException {
        if( channel==null || who == null  ) {
            throw new InvalidPacketException();
        }
        channel = Intermud.getRemoteChannel(channel);
        super.send();
    }

    public String toString() {
		String str = "({\"chan-who-reply\",5,\"" + Server.getMudName() +
		         "\",0,\"" + target_mud + "\",\"" + target_name + "\",\"" + channel + "\",({";
		int i;

		for(i=0; i<who.size(); i++) {
		    String nom = (String)who.elementAt(0);
		    str += "\"" + nom + "\",";
		}
		str += "}),})";
		return str;

    }
}
