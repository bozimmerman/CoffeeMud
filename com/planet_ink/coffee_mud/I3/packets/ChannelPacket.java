package com.planet_ink.coffee_mud.system.I3.packets;
import com.planet_ink.coffee_mud.system.I3.server.Server;


import java.util.Vector;

public class ChannelPacket extends Packet  {
    public String channel = null;
    public String sender_visible_name = null;
    public String message = null;

	public ChannelPacket()
	{
		super();
        type = ChannelPacket.CHAN_MESSAGE;
	}
    public ChannelPacket(Vector v) throws InvalidPacketException {
        super(v);
        try {
            String str = (String)v.elementAt(0);

            if( str.equals("channel-m") ) {
                type = ChannelPacket.CHAN_MESSAGE;
            }
            else if( str.equals("channel-e") ) {
                type = ChannelPacket.CHAN_EMOTE;
            }
            else {
                type = ChannelPacket.CHAN_TARGET;
            }
			channel = (String)v.elementAt(6);
            channel = Intermud.getLocalChannel(channel);
            sender_visible_name = (String)v.elementAt(7);
            message = (String)v.elementAt(8);
        }
        catch( ClassCastException e ) {
            throw new InvalidPacketException();
        }
    }

	
    public ChannelPacket(int t, String chan, String who, String vis, String msg) {
        super();
        type = t;
        channel = chan;
        sender_visible_name = vis;
        sender_name = who;
        message = msg;
    }

    public void send() throws InvalidPacketException {
        if( channel == null || sender_visible_name == null ) {
            throw new InvalidPacketException();
        }
        channel = Intermud.getRemoteChannel(channel);
        message = convertString(message);
        super.send();
    }

    public String toString() {
		String str=
			 "({\"channel-m\",5,\"" + Server.getMudName() + "\",\"" +
               sender_name + "\",0,0,\"" + channel + "\",\"" +
               sender_visible_name + "\",\"" + message + "\",})";
		return str;
    }
}
