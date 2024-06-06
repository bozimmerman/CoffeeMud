package com.planet_ink.coffee_mud.core.intermud.i3.packets;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Vector;

import com.planet_ink.coffee_mud.core.CMath;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.Packet.PacketType;

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
public class IrnData extends IrnPacket
{
	public Packet innerPacket = null;

	public IrnData(final String targetRouter, final MudPacket innerPacket)
	{
		super(targetRouter);
		type = Packet.PacketType.IRN_DATA;
		this.innerPacket = innerPacket;
	}

	public IrnData(final Vector<?> v) throws InvalidPacketException
	{
		super(v);
		type = Packet.PacketType.IRN_DATA;
		if(v.size()>6)
		{
			final List<?> nextPacket = (List<?>)v.get(6);
			if(nextPacket.size()>0)
			{
				final String typeStr = nextPacket.get(0).toString().toUpperCase().replace('-','_');
				final PacketType type = (PacketType)CMath.s_valueOf(PacketType.class, typeStr);
				if(type == null)
				{
					Log.errOut("Unknown data packet type: " + typeStr);
					return;
				}
				final Class<? extends Packet> pktClass = type.packetClass;
				if(pktClass != null)
				{
					try
					{
						final Constructor<? extends Packet> con = pktClass.getConstructor(Vector.class);
						innerPacket = con.newInstance(nextPacket);
					}
					catch( final Exception  e )
					{
						Log.errOut("Error constructing :"+type+" packet:"+e.getMessage());
						Log.debugOut(e);
					}
				}
			}
		}
	}

	@Override
	public void send() throws InvalidPacketException
	{
		if(innerPacket == null)
		{
			throw new InvalidPacketException();
		}
		super.send();
	}

	@Override
	public String toString()
	{
		final String cmd=
				"({\"irn-data\",5," +
				"\"" + sender_router + "\",0," +
				"\"" + target_router + "\",0," +
					innerPacket.toString() +
				",})";
		return cmd;
	}
}
