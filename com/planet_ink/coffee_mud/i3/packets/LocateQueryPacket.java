package com.planet_ink.coffee_mud.i3.packets;

import com.planet_ink.coffee_mud.i3.server.Server;
import com.planet_ink.coffee_mud.utils.*;
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
public class LocateQueryPacket extends Packet {
    public String user_name;

	public LocateQueryPacket()
	{
		super();
        type = Packet.LOCATE_QUERY;
	}
    public LocateQueryPacket(Vector v) throws InvalidPacketException {
        super(v);
        try {
            type = Packet.LOCATE_QUERY;
            user_name = (String)v.elementAt(6);
        }
        catch( ClassCastException e ) {
            throw new InvalidPacketException();
        }
    }

    public LocateQueryPacket(String nom, String who) {
        super();
        type = Packet.LOCATE_QUERY;
        sender_name = nom;
        user_name = who;
    }

    public void send() throws InvalidPacketException {
        if( sender_name == null || user_name == null ) {
            throw new InvalidPacketException();
        }
        super.send();
    }

    public String toString() {
		String str="({\"locate-req\",5,\"" + Server.getMudName() +
               "\",\"" + sender_name + "\",0,0,\"" +
               user_name + "\",})";
		return str;
    }
}
