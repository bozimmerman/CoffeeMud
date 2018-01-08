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
 * Copyright (c) 2010-2018 Bo Zimmerman
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
public class ErrorPacket extends Packet
{
	public String error_code="";
	public String error_message = "";
	public String packetStr = "";

	public ErrorPacket()
	{
		super();
		type = Packet.ERROR_PACKET;
	}

	public ErrorPacket(String to_whom, String mud, String error_code, String error_message, String packetStr)
	{
		super();
		type = Packet.ERROR_PACKET;
		target_mud = mud;
		target_name = to_whom;
		this.error_code=error_code;
		this.error_message=error_message;
		this.packetStr=packetStr;
	}

	public ErrorPacket(Vector v) throws InvalidPacketException
	{
		super(v);
		try
		{
			type = Packet.ERROR_PACKET;
			try
			{
				error_code = v.elementAt(6).toString();
				error_message = v.elementAt(7).toString();
				packetStr=v.elementAt(8).toString();
			}
			catch(final Exception e)
			{
			}
		}
		catch( final ClassCastException e )
		{
			throw new InvalidPacketException();
		}
	}

	@Override
	public void send() throws InvalidPacketException
	{
		super.send();
	}

	@Override
	public String toString()
	{
		final String cmd = "({\"error\",5,\"" + I3Server.getMudName() +
				 "\",0,\"" + target_mud + "\",\"" + target_name + "\"," +
				 "\""+error_code+"\",\""+error_message+"\","+packetStr+",})";
		return cmd;

	}
}
