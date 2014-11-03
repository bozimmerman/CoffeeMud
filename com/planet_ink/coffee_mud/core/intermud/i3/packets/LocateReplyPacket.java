package com.planet_ink.coffee_mud.core.intermud.i3.packets;
import com.planet_ink.coffee_mud.core.intermud.i3.server.*;
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
public class LocateReplyPacket extends Packet {
	public String located_mud_name;
	public String located_visible_name;
	public int    idle_time;
	public String status;

	public LocateReplyPacket(Vector v) throws InvalidPacketException {
		super(v);
		try
		{
			type = Packet.LOCATE_REPLY;
			located_mud_name = (String)v.elementAt(6);
			located_visible_name = (String)v.elementAt(7);
			try
			{
			idle_time = ((Integer)v.elementAt(8)).intValue();
			}
			catch( final ClassCastException e )
			{
				idle_time=-1;
			}
			try
			{
			status = (String)v.elementAt(9);
			}
			catch( final ClassCastException e )
			{
				status="unknown";
			}
		}
		catch( final ClassCastException e )
		{
			throw new InvalidPacketException();
		}
	}

	public LocateReplyPacket(String to_whom, String mud, String who, int idl, String stat)
	{
		super();
		type = Packet.LOCATE_REPLY;
		target_mud = mud;
		target_name = to_whom;
		located_mud_name = I3Server.getMudName();
		located_visible_name = who;
		idle_time = idl;
		status = stat;
	}

	@Override
	public void send() throws InvalidPacketException {
		if( target_name == null || located_mud_name == null ||
			located_visible_name == null || status == null )
			{
			throw new InvalidPacketException();
		}
		super.send();
	}

	@Override
	public String toString()
	{
		return "({\"locate-reply\",5,\"" + I3Server.getMudName() +
			   "\",0,\"" + target_mud + "\",\"" + target_name +
			   "\",\"" + located_mud_name + "\",\"" +
			   located_visible_name + "\"," + idle_time + ",\"" +
			   status + "\",})";
	}
}
