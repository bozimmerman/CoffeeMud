package com.planet_ink.coffee_mud.core.intermud.i3.packets;
import java.util.Vector;

/**
 * Copyright (c) 2024-2025 Bo Zimmerman
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
public class IrnChanlistRequest extends IrnPacket
{
	public int chanlist_id = 0;

	public IrnChanlistRequest(final String targetRouter)
	{
		super(targetRouter);
		type = Packet.PacketType.IRN_CHANLIST_REQ;
	}

	public IrnChanlistRequest(final Vector<?> v) throws InvalidPacketException
	{
		super(v);
		type = Packet.PacketType.IRN_CHANLIST_REQ;
		if(v.size()>6)
			chanlist_id = s_int(v,6);
	}

	@Override
	public String toString()
	{
		final String cmd=
				"({\"irn-chanlist-req\",5," +
				"\"" + sender_router + "\",0," +
				"\"" + target_router + "\",0," + chanlist_id + "," +
				"})";
		return cmd;
	}
}
