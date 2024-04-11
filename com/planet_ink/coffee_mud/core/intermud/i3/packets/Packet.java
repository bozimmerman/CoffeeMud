package com.planet_ink.coffee_mud.core.intermud.i3.packets;
import com.planet_ink.coffee_mud.core.intermud.imc2.*;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.*;
import com.planet_ink.coffee_mud.core.intermud.i3.persist.*;
import com.planet_ink.coffee_mud.core.intermud.i3.server.*;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.MudList;
import com.planet_ink.coffee_mud.core.intermud.i3.net.*;
import com.planet_ink.coffee_mud.core.intermud.*;
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
 * Copyright (c) 1996 George Reese
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
public abstract class Packet
{
	/*
		Transmissions are LPC arrays with a predefined set of six initial elements:
	({ type, ttl, originator mudname, originator username, target mudname, target username, ... }).
	*/
	public static enum PacketType
	{
		CHANNEL_M(ChannelMessage.class),
		CHANNEL_E(ChannelEmote.class),
		CHANNEL_T(ChannelTargetEmote.class),
		WHO_REQ(WhoReqPacket.class),
		WHO_REPLY(WhoReplyPacket.class),
		TELL(TellPacket.class),
		LOCATE_REQ(LocateQueryPacket.class),
		LOCATE_REPLY(LocateReplyPacket.class),
		CHAN_WHO_REQ(ChannelWhoRequest.class),
		CHAN_WHO_REPLY(ChannelWhoReply.class),
		CHANNEL_ADD(ChannelAdd.class),
		CHANNEL_ADMIN(ChannelAdmin.class),
		CHANNEL_REMOVE(ChannelDelete.class),
		CHANNEL_LISTEN(ChannelListen.class),
		CHAN_USER_REQ(ChannelUserRequest.class),
		CHAN_USER_REPLY(ChannelUserReply.class),
		SHUTDOWN(ShutdownPacket.class),
		FINGER_REQ(FingerRequest.class),
		FINGER_REPLY(FingerReply.class),
		PING_REQ(PingPacket.class),
		AUTH_MUD_REQ(MudAuthRequest.class),
		AUTH_MUD_REPLY(MudAuthReply.class),
		UCACHE_UPDATE(UCacheUpdate.class),
		MUDLIST(MudlistPacket.class),
		STARTUP_REPLY(StartupReply.class),
		ERROR(ErrorPacket.class),
		CHANLIST_REPLY(ChanlistReply.class),
		IRN_STARTUP_REQ(IrnStartupRequest.class),
		IRN_MUDLIST_REQ(IrnMudlistRequest.class),
		IRN_MUDLIST_DELTA(IrnMudlistDelta.class),
		IRN_CHANLIST_REQ(IrnChanlistRequest.class),
		IRN_CHANLIST_DELTA(IrnChanlistDelta.class),
		IRN_DATA(IrnData.class),
		IRN_PING(IrnPing.class),
		IRN_SHUTDOWN(IrnShutdown.class),
		STARTUP_REQ_3(StartupReq3.class)
		;
		public Class<? extends Packet> packetClass;
		String key;
		private PacketType(final Class<? extends Packet> pktClass)
		{
			key = this.name().toLowerCase().replace("_", "-");
			if(pktClass != null)
				packetClass = pktClass;
			else
				packetClass = null;
		}
	}

	public Packet()
	{
		super();
	}

	public Packet(final Vector<?> v)
	{
		super();
	}

	public abstract PacketType getType();

	public abstract String convertString(final String cmd);

	public abstract void send() throws InvalidPacketException;
}
