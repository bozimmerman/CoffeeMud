package com.planet_ink.coffee_mud.core.intermud.i3.packets;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.planet_ink.coffee_mud.core.intermud.i3.entities.I3MudX;

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
public class IrnMudlistDelta extends IrnPacket
{
	public int mudlist_id = 0;
	public List<I3MudX> mudlist = new Vector<I3MudX>();

	public IrnMudlistDelta(final String targetRouter)
	{
		super(targetRouter);
		type = Packet.PacketType.IRN_MUDLIST_DELTA;
	}

	public IrnMudlistDelta(final Vector<?> v) throws InvalidPacketException
	{
		super(v);
		type = Packet.PacketType.IRN_MUDLIST_DELTA;
		if(v.size()>6) mudlist_id = ((Integer)v.get(6)).intValue();
		if(v.size()>7)
		{
			@SuppressWarnings("unchecked")
			final Map<String,?> map = (Map<String,?>)v.get(7);
			for(final String name : map.keySet())
			{
				final Object o = map.get(name);
				if(o instanceof Integer)
					continue; // skip the deleted ones.. wtf?
				if(!(o instanceof Map))
					continue; // skip the wrong
				@SuppressWarnings("unchecked")
				final Map<String,?> lst = (Map<String,?>)o;
				final I3MudX mud = new I3MudX(name);
				mud.mudListId = s_int(lst.get("old_mudlist_id"));
				mud.channelListId = s_int(lst.get("old_chanlist_id"));
				mud.player_port = s_int(lst.get("player_port"));
				mud.tcp_port = s_int(lst.get("imud_tcp_port"));
				mud.udp_port = s_int(lst.get("imud_udp_port"));
				mud.mudlib = s_str(lst.get("mudlib"));
				mud.base_mudlib = s_str(lst.get("base_mudlib"));
				mud.driver = s_str(lst.get("driver"));
				mud.mud_type = s_str(lst.get("mud_type"));
				mud.status = s_str(lst.get("open_status"));
				mud.admin_email = s_str(lst.get("admin_email"));
				if(lst.get("services") instanceof Map)
				{
					@SuppressWarnings("unchecked")
					final Map<String,?> m = (Map<String,?>)lst.get("services");
					for(final String str : m.keySet())
					{
						final Object o1 = m.get(str);
						if(o1 instanceof Integer)
							mud.services.put(str, (Integer)o1);
					}
				}
				if(lst.get("other_data") instanceof Map)
				{
					@SuppressWarnings("unchecked")
					final Map<String,?> m = (Map<String,?>)lst.get("other_data");
					for(final String str : m.keySet())
					{
						final Object o1 = m.get(str);
						if(o1 instanceof String)
							mud.other.put(str, (String)o1);
					}
				}
				mud.mud_name = s_str(lst.get("name"));
				mud.address = s_str(lst.get("ip"));
				mud.version = s_int(lst.get("version"));
				mud.connected = s_int(lst.get("restart_delay")) == -1;
				mud.router = s_str(lst.get("router"));
			}
		}
	}

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder();
		str.append("({\"irn-mudlist-delta\",5," +
				"\"" + sender_router + "\",0," +
				"\"" + target_router + "\",0,"+mudlist_id+",");
		str.append("([");
		for(final I3MudX mud : mudlist)
		{
			str.append("\""+mud.mud_name+"\":[(");
			str.append("\"").append("old_mudlist_id").append("\":").append(mud.mudListId).append(","); //0
			str.append("\"").append("old_chanlist_id").append("\":").append(mud.channelListId).append(","); //1
			str.append("\"").append("player_port").append("\":").append(mud.player_port).append(","); //2
			str.append("\"").append("imud_tcp_port").append("\":").append(mud.tcp_port).append(","); //3
			str.append("\"").append("imud_udp_port").append("\":").append(mud.udp_port).append(","); //4
			str.append("\"").append("mudlib").append("\":").append("\"").append(mud.mudlib).append("\","); //5
			str.append("\"").append("base_mudlib").append("\":").append("\"").append(mud.base_mudlib).append("\","); //6
			str.append("\"").append("driver").append("\":").append("\"").append(mud.driver).append("\","); //7
			str.append("\"").append("mud_type").append("\":").append("\"").append(mud.mud_type).append("\","); //8
			str.append("\"").append("open_status").append("\":").append("\"").append(mud.status).append("\","); //9
			str.append("\"").append("admin_email").append("\":").append("\"").append(mud.admin_email).append("\","); //10
			str.append("\"").append("services").append("\":");
			{
				str.append("([");
				for(final String key : mud.services.keySet())
					str.append("\"").append(key).append("\":").append(mud.services.get(key)).append(",");
				str.append("]),");
			}
			str.append("\"").append("other_data").append("\":");
			{
				str.append("([");
				for(final String key : mud.other.keySet())
					str.append("\"").append(key).append("\":\"").append(mud.other.get(key)).append("\",");
				str.append("]),");
			}
			str.append("\"").append("password").append("\":").append(mud.password).append(",");
			str.append("\"").append("connect_time").append("\":").append(mud.connectTime).append(",");
			str.append("\"").append("disconnect_time").append("\":").append(mud.disconnectTime).append(",");
			str.append("\"").append("version").append("\":").append(mud.version).append(",");
			str.append("\"").append("name").append("\":").append("\"").append(mud.mud_name).append("\",");
			str.append("\"").append("ip").append("\":").append("\"").append(mud.address).append("\",");
			str.append("\"").append("version").append("\":").append(mud.version).append(",");
			str.append("\"").append("restart_delay").append("\":").append(mud.connected?-1:0).append(",");
			str.append("\"").append("router").append("\":").append("\"").append(mud.router).append("\",");
			str.append("]),");
		}
		str.append("]),");
		str.append("})");
		return str.toString();
	}
}
