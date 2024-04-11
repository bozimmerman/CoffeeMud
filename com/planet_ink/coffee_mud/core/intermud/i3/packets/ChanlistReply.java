package com.planet_ink.coffee_mud.core.intermud.i3.packets;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.Channel;
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
public class ChanlistReply extends IrnPacket
{
	public int chanlist_id = 0;
	public List<Channel> chanlist = new Vector<Channel>();

	public ChanlistReply(final String targetRouter)
	{
		super(targetRouter);
		type = Packet.PacketType.CHANLIST_REPLY;
	}

	public ChanlistReply(final Vector<?> v) throws InvalidPacketException
	{
		super(v);
		type = Packet.PacketType.CHANLIST_REPLY;
		chanlist_id = s_int(v,6);
		if((v.size()>7)
		&&(v.get(7) instanceof Map))
		{
			@SuppressWarnings("unchecked")
			final Map<String,?> map = (Map<String,?>)v.get(7);
			for(final String name : map.keySet())
			{
				final Channel chan = new Channel();
				chan.channel = name;
				final Object o = map.get(name);
				if(o instanceof Integer)
				{
					chan.modified = Persistent.DELETED;
					chanlist.add(chan);
					continue;
				}
				if(!(o instanceof List))
					continue; // skip the wrong
				final List<?> lst = (List<?>)o;
				if(lst.size()>=2)
				{
					chan.owner = s_str(lst,0);
					chan.type = s_int(lst,1);
				}
				chanlist.add(chan);
			}
		}
	}

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder();
		str.append("({\"chanlist-reply\",5," +
				"\"" + sender_router + "\",0," +
				"\"" + target_router + "\",0,"+chanlist_id+",");
		str.append("([");
		for(final Channel chan : chanlist)
		{
			str.append("\""+chan.channel+"\":({");
			str.append("\"").append(chan.owner).append("\",");//0
			str.append(chan.type).append(","); //1
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
			Log.errOut("Unknown peer target: "+target_router);
			return;
		}
		I3Router.writePacket(this, mudPeer);
	}
}
