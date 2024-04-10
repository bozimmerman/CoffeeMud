package com.planet_ink.coffee_mud.core.intermud.i3.packets;
import com.planet_ink.coffee_mud.core.intermud.i3.Intermud;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.I3Mud;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.I3MudX;
import com.planet_ink.coffee_mud.core.intermud.i3.net.NetPeer;
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

import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

/**
 * Copyright (c) 2010-2024 Bo Zimmerman
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
public class StartupReq3 extends IrnPacket
{
	public int		password		= 0;
	public int		mudListId		= 0;
	public int		channelListId	= 0;
	public int		port			= 0;
	public int		tcpPort			= 0;
	public int		udpPort			= 0;
	public String	lib				= "";
	public String	baseLib			= "";
	public String	driver			= "";
	public String	mtype			= "";
	public String	mudState		= "";
	public String	adminEmail		= "";

	public Map<String,Integer> services = new Hashtable<String,Integer>();
	public Map<String,String> other = new Hashtable<String,String>();

	public StartupReq3(final String targetRouter)
	{
		super(targetRouter);
		super.sender_router = I3Server.getMudName();
		type = Packet.PacketType.STARTUP_REQ_3;
	}

	public StartupReq3(final Vector<?> v)
	{
		super(v);
		type = Packet.PacketType.STARTUP_REQ_3;
		try
		{
			if(v.size()>6)
				this.password = ((Integer)v.get(6)).intValue();
			if(v.size()>7)
				this.mudListId = ((Integer)v.get(7)).intValue();
			if(v.size()>8)
				this.channelListId = ((Integer)v.get(8)).intValue();
			if(v.size()>9)
				this.port = ((Integer)v.get(9)).intValue();
			if(v.size()>10)
				this.tcpPort = ((Integer)v.get(10)).intValue();
			if(v.size()>11)
				this.udpPort = ((Integer)v.get(11)).intValue();
			if(v.size()>12)
				this.lib = (String)v.get(12);
			if(v.size()>13)
				this.baseLib = (String)v.get(13);
			if(v.size()>14)
				this.driver = (String)v.get(14);
			if(v.size()>15)
				this.mtype = (String)v.get(15);
			if(v.size()>16)
				this.mudState = (String)v.get(16);
			if(v.size()>17)
				this.adminEmail = (String)v.get(17);
			if(v.size()>18)
			{
				@SuppressWarnings("unchecked")
				final
				Map<Object,Object> map = (Map<Object,Object>)v.get(18);
				this.services.clear();
				for(final Object key : map.keySet())
				{
					final Object o = map.get(key);
					if(o instanceof Integer)
						this.services.put(key.toString(), (Integer)o);
				}
			}
			if(v.size()>19)
			{
				@SuppressWarnings("unchecked")
				final
				Map<Object,Object> map = (Map<Object,Object>)v.get(19);
				this.other.clear();
				for(final Object key : map.keySet())
				{
					final Object o = map.get(key);
					this.other.put(key.toString(),o.toString());
				}
			}
		}
		catch(final ClassCastException e)
		{
		}
	}

	public StartupReq3(final String targetRouter, final int password,
					  final int mudListId, final int channelListId,
					  final int port, final int tcpPort, final int udpPort,
					  final String lib, final String baseLib, final String driver, final String mtype,
					  final String mudState, final String adminEmail,
					  final Map<String,Integer> services, final Map<String,String> other)
	{
		super(targetRouter);
		super.sender_router = I3Server.getMudName();
		type = Packet.PacketType.STARTUP_REQ_3;
		this.password = password;
		this.mudListId = mudListId;
		this.channelListId = channelListId;
		this.port = port;
		this.tcpPort = tcpPort;
		this.udpPort = udpPort;
		this.lib = lib;
		this.baseLib = baseLib;
		this.driver = driver;
		this.mtype = mtype;
		this.mudState = mudState;
		this.adminEmail = adminEmail;
		this.services.clear();
		if(services != null)
			this.services.putAll(services);
		this.other.clear();
		if(other != null)
			this.other.putAll(other);
	}

	public I3MudX makeMud(final NetPeer peer)
	{
		final I3MudX mud = new I3MudX(this.sender_router);
		String remoteAddr = peer.getSocket().getRemoteSocketAddress().toString();
		if(remoteAddr.startsWith("/"))
			remoteAddr=remoteAddr.substring(1);
		final int x = remoteAddr.indexOf(':');
		mud.address = (x>0) ? remoteAddr.substring(0,x) : remoteAddr;
		mud.admin_email = this.adminEmail;
		mud.base_mudlib = this.baseLib;
		mud.channelListId = this.channelListId;
		mud.driver = this.driver;
		mud.modified = (int)(System.currentTimeMillis()/1000);
		mud.mud_name = this.sender_router;
		mud.mud_type = this.mtype;
		mud.mudlib = this.lib;
		mud.mudListId = this.mudListId;
		mud.other.putAll(this.other);
		mud.password = -1;
		mud.player_port = this.port;
		mud.services.putAll(this.services);
		mud.state = 1;
		mud.status = this.mudState;
		mud.tcp_port = this.tcpPort;
		mud.udp_port = this.udpPort;
		return mud;
	}

	@Override
	public void send() throws InvalidPacketException
	{
		super.send();
	}

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder(
				"({\"startup-req-3\",5,\"" + sender_router + "\",0,\"" +
				target_router + "\",0," + password +
				 "," + mudListId + "," + channelListId + "," + port + "," + tcpPort +"," +
				 udpPort+",\""+lib+"\",\""+baseLib+"\",\""+driver+"\",\""+mtype+"\"," +
				 "\""+mudState+"\",\""+adminEmail+"\",");
		str.append("([");
		if((this.services != null)&&(this.services.size()>0))
		{
			for(final String key : this.services.keySet())
				str.append("\""+key+"\":"+this.services.get(key).toString()+",");
		}
		str.append("]),([");
		if((this.other != null)&&(this.other.size()>0))
		{
			for(final String key : this.other.keySet())
				str.append("\""+key+"\":\""+this.other.get(key).toString()+"\",");
		}
		str.append("]),})");
		return str.toString();
	}
}
