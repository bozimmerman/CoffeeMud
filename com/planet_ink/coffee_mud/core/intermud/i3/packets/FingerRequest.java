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
 * Copyright (c) 2010-2025 Bo Zimmerman
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
public class FingerRequest extends MudPacket
{
	public FingerRequest()
	{
		super();
		type = Packet.PacketType.FINGER_REQ;
	}

	public FingerRequest(final Vector<?> v) throws InvalidPacketException
	{
		super(v);
		try
		{
			type = Packet.PacketType.FINGER_REQ;
			target_mud=(String)v.elementAt(4);
			target_name=(String)v.elementAt(6);
		}
		catch( final ClassCastException e )
		{
			throw new InvalidPacketException();
		}
	}

	@Override
	public void send() throws InvalidPacketException
	{
		if( sender_name == null || target_mud == null || sender_mud == null  || target_name == null)
		{
			throw new InvalidPacketException();
		}
		super.send();
	}

	@Override
	public String toString()
	{
		final String cmd="({\"finger-req\",5,\"" + sender_mud +
			   "\",\"" + sender_name + "\",\"" + target_mud + "\",0,\"" + target_name + "\",})";
		return cmd;
	}
}
