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
public class MudlistPacket extends IrnPacket
{
	public int mudlist_id = 0;
	public List<I3MudX> mudlist = new Vector<I3MudX>();

	public MudlistPacket(final String targetRouter)
	{
		super(targetRouter);
		type = Packet.PacketType.MUDLIST;
	}

	public MudlistPacket(final Vector<?> v) throws InvalidPacketException
	{
		super(v);
		type = Packet.PacketType.MUDLIST;
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
				if(!(o instanceof List))
					continue; // skip the wrong
				final List<?> lst = (List<?>)o;
				final I3MudX mud = new I3MudX(name);
				if(lst.size() > 0)
					mud.mudListId = ((Integer)lst.get(0)).intValue();
				if(lst.size() > 1)
					mud.channelListId = ((Integer)lst.get(1)).intValue();
				if(lst.size() > 2)
					mud.player_port = ((Integer)lst.get(2)).intValue();
				if(lst.size() > 3)
					mud.tcp_port = ((Integer)lst.get(3)).intValue();
				if(lst.size() > 4)
					mud.udp_port = ((Integer)lst.get(4)).intValue();
				if(lst.size() > 5)
					mud.mudlib = ((String)lst.get(5));
				if(lst.size() > 6)
					mud.base_mudlib = ((String)lst.get(6));
				if(lst.size() > 7)
					mud.driver = ((String)lst.get(7));
				if(lst.size() > 8)
					mud.mud_type = ((String)lst.get(8));
				if(lst.size() > 9)
					mud.status = ((String)lst.get(9));
				if(lst.size() > 10)
					mud.admin_email = ((String)lst.get(10));
				if(lst.size() > 11)
				{
					@SuppressWarnings("unchecked")
					final Map<String,?> m = (Map<String,?>)lst.get(11);
					for(final String str : m.keySet())
					{
						final Object o1 = m.get(str);
						if(o1 instanceof Integer)
							mud.services.put(str, (Integer)o1);
					}
				}
				if(lst.size() > 12)
				{
					@SuppressWarnings("unchecked")
					final Map<String,?> m = (Map<String,?>)lst.get(12);
					for(final String str : m.keySet())
					{
						final Object o1 = m.get(str);
						if(o1 instanceof String)
							mud.other.put(str, (String)o1);
					}
				}
			}
		}
	}

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder();
		str.append("({\"mudlist\",5," +
				"\"" + sender_router + "\",0," +
				"\"" + target_router + "\",0,"+mudlist_id+",");
		str.append("([");
		for(final I3MudX mud : mudlist)
		{
			str.append("\""+mud.mud_name+"\":[{");
			str.append(mud.connected?-1:0).append(","); //1
			str.append("\"").append(mud.address).append("\",");//2
			str.append(mud.player_port).append(","); //3
			str.append(mud.tcp_port).append(","); //4
			str.append(mud.udp_port).append(","); //5
			str.append("\"").append(mud.mudlib).append("\","); //6
			str.append("\"").append(mud.base_mudlib).append("\","); //7
			str.append("\"").append(mud.driver).append("\","); //8
			str.append("\"").append(mud.mud_type).append("\","); //9
			str.append("\"").append(mud.status).append("\","); //10
			str.append("\"").append(mud.admin_email).append("\","); //11
			{
				str.append("([");
				for(final String key : mud.services.keySet())
					str.append("\"").append(key).append("\":").append(mud.services.get(key)).append(",");
				str.append("]),");
			}
			{
				str.append("([");
				for(final String key : mud.other.keySet())
					str.append("\"").append(key).append("\":\"").append(mud.other.get(key)).append("\",");
				str.append("]),");
			}
			str.append("}],");
		}
		str.append("]),");
		str.append("})");
		return str.toString();
	}
}
