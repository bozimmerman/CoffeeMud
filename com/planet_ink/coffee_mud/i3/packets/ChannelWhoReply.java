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
			try{
			who = (Vector)v.elementAt(7);
			}catch(Exception e){ who=new Vector();}
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
