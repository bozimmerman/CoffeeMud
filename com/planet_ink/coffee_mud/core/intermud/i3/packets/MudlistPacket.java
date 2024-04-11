package com.planet_ink.coffee_mud.core.intermud.i3.packets;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.I3RMud;
import com.planet_ink.coffee_mud.core.intermud.i3.persist.Persistent;
import com.planet_ink.coffee_mud.core.intermud.i3.router.I3Router;
import com.planet_ink.coffee_mud.core.intermud.i3.router.MudPeer;

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
	public List<I3RMud> mudlist = new Vector<I3RMud>();

	public MudlistPacket(final String targetRouter)
	{
		super(targetRouter);
		type = Packet.PacketType.MUDLIST;
	}

	public MudlistPacket(final Vector<?> v) throws InvalidPacketException
	{
		super(v);
		type = Packet.PacketType.MUDLIST;
		mudlist_id = s_int(v,6);
		if((v.size()>7)
		&&(v.get(7) instanceof Map))
		{
			@SuppressWarnings("unchecked")
			final Map<String,?> map = (Map<String,?>)v.get(7);
			for(final String name : map.keySet())
			{
				final I3RMud mud = new I3RMud(name);
				final Object o = map.get(name);
				if(o instanceof Integer)
				{
					mud.modified = Persistent.DELETED;
					mudlist.add(mud);
					continue;
				}
				if(!(o instanceof List))
					continue; // skip the wrong
				final List<?> lst = (List<?>)o;
				mud.state = s_int(lst,0);
				mud.address = s_str(lst,1);
				mud.player_port = s_int(lst,2);
				mud.tcp_port = s_int(lst,3);
				mud.udp_port = s_int(lst,4);
				mud.mudlib = s_str(lst,5);
				mud.base_mudlib = s_str(lst,6);
				mud.driver = s_str(lst,7);
				mud.mud_type = s_str(lst,8);
				mud.status = s_str(lst,9);
				mud.admin_email = s_str(lst,10);
				if((lst.size() > 11)
				&&(lst.get(11) instanceof Map))
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
				if((lst.size() > 12)
				&&(lst.get(12) instanceof Map))
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
				mudlist.add(mud);
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
		for(final I3RMud mud : mudlist)
		{
			str.append("\""+mud.mud_name+"\":({");
			str.append(mud.state<0?-1:0).append(","); //1
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
			str.append("}),");
		}
		str.append("]),");
		str.append("})");
		return str.toString();
	}

	public void send() throws InvalidPacketException
	{
		final MudPeer mudPeer = I3Router.findMudPeer(this.target_router);
		if(mudPeer == null)
		{
			Log.errOut("Unknown mud target: "+target_router);
			return;
		}
		I3Router.writePacket(this, mudPeer);
	}
}
