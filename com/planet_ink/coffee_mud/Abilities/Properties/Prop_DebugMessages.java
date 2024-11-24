package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.Abilities.interfaces.Ability;
import com.planet_ink.coffee_mud.Common.interfaces.CMMsg;
import com.planet_ink.coffee_mud.Locales.interfaces.Room;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.interfaces.Environmental;

import java.util.Enumeration;

/*
   Copyright 2024 github.com/toasted323

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

public
class Prop_DebugMessages extends Property
{
	@Override
	public String ID()
	{
		return "Prop_DebugMessages";
	}

	@Override
	public String name()
	{
		return "Room Debug Messages";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS;
	}

	@Override
	public String accountForYourself()
	{
		return "This is a property to debug messages passing through rooms.";

	}

	@Override
	public boolean okMessage(final Environmental myHost,final CMMsg msg)
	{
		return super.okMessage(myHost,msg);
	}

	@Override
	public void executeMsg(final Environmental myHost,final CMMsg msg)
	{
		if(myHost instanceof Room)
		{
			Room room = (Room) myHost;
			for(Enumeration<MOB> e = room.inhabitants(); e.hasMoreElements(); )
			{
				MOB mob = e.nextElement();
				if(mob != null && mob.isPlayer())
				{
					StringBuilder debugMessage = new StringBuilder();
					debugMessage.append("Debug: Message received in Prop_DebugMessages:\n");
					debugMessage.append("Source: ").append(msg.source() != null ? msg.source().name() : "None").append("\n");
					debugMessage.append("Target: ").append(msg.target() != null ? msg.target().name() : "None").append("\n");
					debugMessage.append("Tool: ").append(msg.tool() != null ? msg.tool().name() : "None").append("\n");
					debugMessage.append("Source Message: ").append(msg.sourceMessage()).append("\n");
					debugMessage.append("Target Message: ").append(msg.targetMessage()).append("\n");
					debugMessage.append("Others Message: ").append(msg.othersMessage()).append("\n");
					debugMessage.append("Type: ").append(msg.sourceMinor()).append("/").append(msg.targetMinor()).append("/").append(msg.othersMinor()).append("\n");
					mob.tell(debugMessage.toString());
				}
			}
		}
		super.executeMsg(myHost,msg);
	}
}