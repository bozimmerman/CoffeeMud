package com.planet_ink.coffee_mud.system.I3.packets;

import com.planet_ink.coffee_mud.system.I3.server.Server;
import com.planet_ink.coffee_mud.utils.*;
import java.util.Vector;

public class ChannelWhoRequest extends Packet {
    public String channel = null;

	public ChannelWhoRequest()
	{
		super();
        type = Packet.CHAN_WHO_REQ;
	}
    public ChannelWhoRequest(Vector v) throws InvalidPacketException {
        super(v);
        try {
            type = Packet.CHAN_WHO_REQ;
			channel = (String)v.elementAt(6);
            channel = Intermud.getLocalChannel(channel);
        }
        catch( ClassCastException e ) {
            throw new InvalidPacketException();
        }
    }

    public void send() throws InvalidPacketException {
        if( sender_name == null || target_mud == null || sender_mud == null  || channel == null) {
            throw new InvalidPacketException();
        }
        channel = Intermud.getRemoteChannel(channel);
        super.send();
    }

    public String toString() {
		String str="({\"chan-who-req\",5,\"" + Server.getMudName() +
               "\",\"" + sender_name + "\",\"" + target_mud + "\",0,\"" + channel + "\",})";
		return str;
    }
}
