package com.planet_ink.coffee_mud.core.intermud.i3.packets;
import com.planet_ink.coffee_mud.core.intermud.i3.server.*;
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
public class ChannelTargetEmote extends ChannelPacket
{
	public String message_target = null;
	public String target_visible_name = null;

	public ChannelTargetEmote()
	{
		super();
		type = Packet.PacketType.CHANNEL_T;
	}

	public ChannelTargetEmote(final Vector<?> v) throws InvalidPacketException
	{
		super(v);
		try
		{
			type = Packet.PacketType.CHANNEL_T;
			target_mud=(String)v.elementAt(7);
			target_name=(String)v.elementAt(8);
			message=(String)v.elementAt(9);
			message_target=(String)v.elementAt(10);
			sender_visible_name = (String)v.elementAt(11);
			target_visible_name = (String)v.elementAt(12);
		}
		catch( final ClassCastException e )
		{
			throw new InvalidPacketException();
		}
	}

	public ChannelTargetEmote(final int t, final String chan, final String who, final String vis, final String msg)
	{
		super(Packet.PacketType.CHANNEL_T,chan,who,vis,msg);
	}

	@Override
	public String toString()
	{
		String cmd=null;
		cmd="({\"channel-t\",5,\"" + sender_mud + "\",\"" +
		 sender_name + "\",0,0,\"" + channel + "\",\"" +
		 target_mud + "\",\"" + target_name + "\",\"" +
		 message + "\",\"" + message_target + "\",\"" +
		 sender_visible_name + "\",\"" + target_visible_name + "\",})";
		return cmd;
	}
}
