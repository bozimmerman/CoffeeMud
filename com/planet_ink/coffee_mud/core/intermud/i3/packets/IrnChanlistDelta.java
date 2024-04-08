package com.planet_ink.coffee_mud.core.intermud.i3.packets;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.planet_ink.coffee_mud.core.intermud.i3.entities.Channel;

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
public class IrnChanlistDelta extends IrnPacket
{
	public int chanlist_id = 0;
	public List<Channel> chanlist = new Vector<Channel>();

	public IrnChanlistDelta(final String targetRouter)
	{
		super(targetRouter);
		type = Packet.PacketType.IRN_CHANLIST_DELTA;
	}

	public IrnChanlistDelta(final Vector<?> v) throws InvalidPacketException
	{
		super(v);
		type = Packet.PacketType.IRN_CHANLIST_DELTA;
		if(v.size()>6) chanlist_id = ((Integer)v.get(6)).intValue();
		if(v.size()>7)
		{
			@SuppressWarnings("unchecked")
			final Map<String,?> map = (Map<String,?>)v.get(7);
			if(map.containsKey("channels"))
			{
				final Object o = map.get("channels");
				if(o instanceof Map)
				{
					@SuppressWarnings("unchecked")
					final Map<String,?> cmap = (Map<String,?>)o;
					for(final String channame : cmap.keySet())
					{
						final Object o1 = cmap.get(channame);
						if(o1 instanceof List)
						{
							final List<?> l = (List<?>)o1;
							final Channel c = new Channel();
							c.channel = channame;
							c.type = s_int(l.get(0));
							c.owner = s_str(l.get(1));
							chanlist.add(c);
							// spot 2 is a list with "ban/allow list", whatever that is.
						}
					}
				}
			}
		}
	}

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder("");
		str.append("({\"irn-chanlist-delta\",5," +
				"\"" + sender_router + "\",0," +
				"\"" + target_router + "\",0,"+chanlist_id+",");
		str.append("([");
		str.append("\"channels\":([");
		for(final Channel c : chanlist)
		{
			str.append("\""+c.channel+"\":({");
			str.append(c.type).append(",");
			str.append("\"").append(c.owner).append("\",");
			str.append("({}),");
			str.append("}),");
		}
		str.append("]),");
		str.append("\"listening\":([]),");
		str.append("]),");
		str.append("})");
		return str.toString();
	}
}
