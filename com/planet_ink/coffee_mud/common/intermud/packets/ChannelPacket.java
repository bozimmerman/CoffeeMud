package com.planet_ink.coffee_mud.i3.packets;
import com.planet_ink.coffee_mud.i3.server.Server;


import java.util.Vector;

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
public class ChannelPacket extends Packet  {
    public String channel = null;
    public String sender_visible_name = null;
    public String message = null;
    public String message_target = null;
    public String target_visible_name = null;

	public ChannelPacket()
	{
		super();
        type = ChannelPacket.CHAN_MESSAGE;
	}
    public ChannelPacket(Vector v) throws InvalidPacketException {
        super(v);
        try {
            String str = (String)v.elementAt(0);

			channel = (String)v.elementAt(6);
            channel = Intermud.getLocalChannel(channel);
            if( str.equals("channel-e") ) 
            {
                type = ChannelPacket.CHAN_EMOTE;
                sender_visible_name = (String)v.elementAt(7);
                message = (String)v.elementAt(8);
            }
            else
            if( str.equals("channel-t") ) 
            {
                type = ChannelPacket.CHAN_TARGET;
                target_mud=(String)v.elementAt(7);
                target_name=(String)v.elementAt(8);
                message=(String)v.elementAt(9);
                message_target=(String)v.elementAt(10);
                sender_visible_name = (String)v.elementAt(11);
                target_visible_name = (String)v.elementAt(12);
            }
            else
            {
                type = ChannelPacket.CHAN_MESSAGE;
                sender_visible_name = (String)v.elementAt(7);
                message = (String)v.elementAt(8);
            }
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
		String str=null;
        if(type==CHAN_TARGET)
			 str="({\"channel-t\",5,\"" + Server.getMudName() + "\",\"" +
             sender_name + "\",0,0,\"" + channel + "\",\"" +
             target_mud + "\",\"" + target_name + "\",\"" +
             message + "\",\"" + message_target + "\",\"" +
             sender_visible_name + "\",\"" + target_visible_name + "\",})";
        else
        if(type==CHAN_EMOTE)
			 str="({\"channel-e\",5,\"" + Server.getMudName() + "\",\"" +
             sender_name + "\",0,0,\"" + channel + "\",\"" +
             sender_visible_name + "\",\"" + message + "\",})";
        else
			 str="({\"channel-m\",5,\"" + Server.getMudName() + "\",\"" +
               sender_name + "\",0,0,\"" + channel + "\",\"" +
               sender_visible_name + "\",\"" + message + "\",})";
		return str;
    }
}
