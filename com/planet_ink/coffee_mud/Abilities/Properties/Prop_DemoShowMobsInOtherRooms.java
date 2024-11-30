package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.Abilities.interfaces.Ability;
import com.planet_ink.coffee_mud.Common.interfaces.CMMsg;
import com.planet_ink.coffee_mud.Locales.interfaces.Room;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.CMClass;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.Directions;
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

public class Prop_DemoShowMobsInOtherRooms extends LoggableProperty
{
	@Override
	public String ID()
	{
		return "Prop_DemoShowMobsInOtherRooms";
	}

	@Override
	public String name()
	{
		return "Show Mobs in Adjacent Rooms";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS;
	}

	@Override
	public String accountForYourself()
	{
		return L("This property shows mobs in adjacent rooms.");
	}

	@Override
	protected void handleParsedConfiguration()
	{
		super.handleParsedConfiguration();
		logger.logInfo("Configuration parsed");
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		logger.logDebug("Received okMessage: " + msg.toString());
		return super.okMessage(myHost, msg);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		logger.logDebug("Executing message: " + msg.toString());
		if(myHost instanceof Room && msg.targetMinor() == CMMsg.TYP_LOOK)
		{
			Room room = (Room) myHost;
			StringBuilder mobInfo = new StringBuilder("\n"+L("You sense the presence of creatures in nearby rooms:"+"\n"));

			for(int d = Directions.NUM_DIRECTIONS(); d >= 0; d--)
			{
				Room adjacentRoom = room.getRoomInDir(d);
				if(adjacentRoom != null)
				{
					mobInfo.append(CMLib.directions().getDirectionName(d)).append(": ");
					boolean foundMobs = false;
					for(Enumeration<MOB> e = adjacentRoom.inhabitants(); e.hasMoreElements(); )
					{
						MOB mob = e.nextElement();
						if(mob != null)
						{
							mobInfo.append(mob.name());
							if(e.hasMoreElements()) mobInfo.append(", ");
							foundMobs = true;
						}
					}
					if(!foundMobs)
					{
						mobInfo.append(L("None"));
					}
					mobInfo.append("\n");
				}
			}

			logger.logInfo("Adding mob information to look message: " + mobInfo);
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,CMMsg.MSG_OK_VISUAL,mobInfo.toString(),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
		}
		super.executeMsg(myHost,msg);
	}
}