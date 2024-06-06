package com.planet_ink.coffee_mud.core.intermud.i3.packets;
import com.planet_ink.coffee_mud.core.intermud.i3.server.I3Server;
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
 * Copyright (c) 2022-2024 Bo Zimmerman
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
public class UCacheUpdate extends MudPacket
{
	public String	uname1	= "";
	public String	uname2	= "";
	public int		gender	= 0;

	public UCacheUpdate()
	{
		super();
		type = Packet.PacketType.UCACHE_UPDATE;
	}

	public UCacheUpdate(final Vector<?> v)
	{
		super(v);
		type = Packet.PacketType.UCACHE_UPDATE;
		uname1=(String)v.elementAt(6);
		uname2=(String)v.elementAt(7);
		gender=CMath.s_int(v.elementAt(8).toString());
	}

	public UCacheUpdate(final String uname1, final String uname2, final int gender)
	{
		super();
		type = Packet.PacketType.UCACHE_UPDATE;
		this.uname1=uname1;
		this.uname2=uname2;
		this.gender=gender;
	}

	@Override
	public void send() throws InvalidPacketException
	{
		super.send();
	}

	@Override
	public String toString()
	{
		return "({\"ucache-update\",5,\""+sender_mud+"\",0,0,0,"
				+ "\""+uname1+"\",\""+uname2+"\","+gender+",})";
	}
}
