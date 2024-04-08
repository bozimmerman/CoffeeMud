package com.planet_ink.coffee_mud.core.intermud.i3.packets;
import com.planet_ink.coffee_mud.core.intermud.i3.Intermud;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.NameServer;
import com.planet_ink.coffee_mud.core.intermud.i3.router.I3Router;
import com.planet_ink.coffee_mud.core.intermud.i3.router.IRouterPeer;
import com.planet_ink.coffee_mud.core.intermud.i3.server.I3Server;
import com.planet_ink.coffee_mud.core.intermud.i3.server.ServerObject;
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

import java.util.List;
import java.util.Vector;

/**
 * Copyright (c) 2024-2024 Bo Zimmerman
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
public class StartupReply extends IrnPacket
{
	public final List<List<String>> routers = new Vector<List<String>>();
	public int password = -1;

	public StartupReply(final String targetRouter)
	{
		super(targetRouter);
		type = Packet.PacketType.STARTUP_REPLY;
		final NameServer me = I3Router.getNameServer();
		routers.add(new XVector<String>( me.name, me.ip + " " + me.port ));
		for(final IRouterPeer obj : I3Router.getRouterPeers())
		{
			if(!obj.name.equals(me.name))
				routers.add(new XVector<String>( obj.name, obj.ip + " " + obj.port ));
		}
	}

	@SuppressWarnings("unchecked")
	public StartupReply(final Vector<?> v)
	{
		super(v);
		type = Packet.PacketType.STARTUP_REPLY;
		if(v.size()>6)
		{
			routers.clear();
			routers.addAll((List<List<String>>)v.elementAt(6));
		}
		if(v.size()>7)
			password = ((Integer)v.elementAt(7)).intValue();
	}

	public StartupReply(final String targetMud, final List<List<String>> routers, final int password)
	{
		super(targetMud);
		type = Packet.PacketType.STARTUP_REPLY;
		this.routers.clear();
		this.routers.addAll(routers);
		this.password = password;
	}

	@Override
	public void send() throws InvalidPacketException
	{
		if( target_router==null || routers.size()==0  )
		{
			throw new InvalidPacketException();
		}
		super.send();
	}

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder();
		str.append("({\"startup-reply\",5,\""+sender_router+"\",0,\""+target_router+"\",0,({");
		for(final List<String> router : routers )
		{
			if(router.size()>=2)
				str.append("({\""+router.get(0)+"\",\""+router.get(1)+"\",}),");
		}
		str.append("}),"+password+",})");
		return str.toString();
	}
}
