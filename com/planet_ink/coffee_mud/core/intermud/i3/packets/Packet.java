package com.planet_ink.coffee_mud.core.intermud.i3.packets;
import com.planet_ink.coffee_mud.core.intermud.imc2.*;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.*;
import com.planet_ink.coffee_mud.core.intermud.i3.persist.*;
import com.planet_ink.coffee_mud.core.intermud.i3.server.*;
import com.planet_ink.coffee_mud.core.intermud.i3.net.*;
import com.planet_ink.coffee_mud.core.intermud.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.Vector;

/**
 * Copyright (c) 1996 George Reese
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
@SuppressWarnings("rawtypes")
public class Packet {
	/*
		Transmissions are LPC arrays with a predefined set of six initial elements:
	({ type, ttl, originator mudname, originator username, target mudname, target username, ... }).
	*/
	final static public int CHAN_MESSAGE = 1;
	final static public int CHAN_EMOTE   = 2;
	final static public int CHAN_TARGET  = 3;
	final static public int WHO_REQUEST  = 4;
	final static public int WHO_REPLY    = 5;
	final static public int TELL		 = 6;
	final static public int LOCATE_QUERY = 7;
	final static public int LOCATE_REPLY = 8;
	final static public int CHAN_WHO_REQ = 9;
	final static public int CHAN_WHO_REP = 10;
	final static public int CHAN_ADD	 = 11;
	final static public int CHAN_REMOVE  = 12;
	final static public int CHAN_LISTEN  = 13;
	final static public int CHAN_USER_REQ= 14;
	final static public int CHAN_USER_REP= 15;
	final static public int SHUTDOWN	 = 16;
	final static public int FINGER_REQUEST=17;
	final static public int FINGER_REPLY = 18;
	final static public int ERROR_PACKET = 19;
	final static public int PING_PACKET  = 20;
	final static public int MAUTH_REQUEST=21;
	final static public int MAUTH_REPLY  = 22;

	public String sender_mud = null;
	public String sender_name = null;
	public String target_mud = null;
	public String target_name = null;
	public int    type = 0;

	public Packet()
	{
		super();
		sender_mud = I3Server.getMudName();
	}

	public Packet(Vector v)
	{
		super();
		{
			Object ob;

			ob = v.elementAt(2);
			if( ob instanceof String )
			{
				sender_mud = (String)ob;
			}
			ob = v.elementAt(3);
			if( ob instanceof String )
			{
				sender_name = (String)ob;
			}
			ob = v.elementAt(4);
			if( ob instanceof String )
			{
				target_mud = (String)ob;
			}
			ob = v.elementAt(5);
			if( ob instanceof String )
			{
				target_name = (String)ob;
			}
		}
	}

	public String convertString(String cmd)
	{
		final StringBuffer b = new StringBuffer(cmd);
		int i = 0;

		while( i < b.length() )
		{
			final char c = b.charAt(i);

			if( c != '\\' && c != '"' )
			{
				i++;
			}
			else
			{
				b.insert(i, '\\');
				i += 2;
			}
		}
		return new String(b);
	}

	public void send() throws InvalidPacketException {
		if( type == 0 )
		{
			throw new InvalidPacketException();
		}
		Intermud.sendPacket(this);
	}
}
