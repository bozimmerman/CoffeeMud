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
public class WhoPacket extends Packet {
    public Vector who = null;

	public WhoPacket()
	{
		super();
        type = Packet.WHO_REQUEST;
	}
	
    public WhoPacket(Vector v) throws InvalidPacketException {
        super(v);
        if( v.size() == 6 ) {
            type = Packet.WHO_REQUEST;
        }
        else {
            type = Packet.WHO_REPLY;
            who = (Vector)v.elementAt(6);
        }
    }

    public void send() throws InvalidPacketException {
        if( type == Packet.WHO_REPLY && who == null ) {
            throw new InvalidPacketException();
        }
        super.send();
    }

    public String toString() {
		if(type==Packet.WHO_REQUEST)
		{
			return "({\"who-req\",5,\"" + Server.getMudName() +
			       "\",\"" + sender_name + "\",\"" + target_mud +
			       "\",0,})";
		}
		else
		{
			String str = "({\"who-reply\",5,\"" + Server.getMudName() +
		             "\",0,\"" + target_mud + "\",\"" + target_name + "\",({";
			int i;

			for(i=0; i<who.size(); i++) {
			    Vector v = (Vector)who.elementAt(i);
			    String nom = (String)v.elementAt(0);
			    int idle = ((Integer)v.elementAt(1)).intValue();
			    String xtra = (String)v.elementAt(2);

			    str += "({\"" + nom + "\"," + idle + ",\"" + xtra + "\",}),";
			}
			str += "}),})";
			return str;
		}

    }
}
