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
