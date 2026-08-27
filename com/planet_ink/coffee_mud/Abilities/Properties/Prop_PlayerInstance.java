package com.planet_ink.coffee_mud.Abilities.Properties;
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

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/*
   Copyright 2026-2026 Bo Zimmerman

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
public class Prop_PlayerInstance extends Property implements TriggeredAffect
{
	private static final AtomicInteger instIDNum = new AtomicInteger(0);

	@Override
	public String ID()
	{
		return "Prop_PlayerInstance";
	}

	@Override
	public String name()
	{
		return "Player Instance";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS;
	}

	@Override
	public int triggerMask()
	{
		return TriggeredAffect.TRIGGER_ENTER;
	}

	private Area findExistingInstance(final String suffix)
	{
		final String subSuffix = "_" + suffix;
		for(final Enumeration<Area> e = CMLib.map().areas(); e.hasMoreElements();)
		{
			final Area A = e.nextElement();
			if(CMath.bset(A.flags(), Area.FLAG_PLAYER_INSTANCE)
			&& A.Name().endsWith(subSuffix))
				return A;
		}
		return null;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected != null)
		&& (msg.target() instanceof Room)
		&& (msg.targetMinor() == CMMsg.TYP_ENTER)
		&& (msg.amITarget(affected)))
		{
			final Room targetR = (Room) affected;
			final MOB mob = msg.source();
			final MOB owner = mob.getGroupLeader();
			final String hostRoomID = CMLib.map().getExtendedRoomID(targetR);

			final String subAreaName = owner.Name() + "_" + CMStrings.replaceAll(hostRoomID, "#", "_");
			final Area existingInstA = findExistingInstance(subAreaName);
			if(existingInstA != null && !existingInstA.amDestroyed())
			{
				final String instanceRoomID = existingInstA.Name() + "#0";
				final Room instanceRoom = existingInstA.getRoom(instanceRoomID);
				if(instanceRoom != null)
				{
					msg.setTarget(instanceRoom);
					return super.okMessage(myHost, msg);
				}
			}

			final String areaName = instIDNum.incrementAndGet() + "_" + subAreaName;

			final Area instA = CMClass.getAreaType("PlayerInstanceArea");
			instA.setName(areaName);
			((PlayerOwned)instA).setOwnerName(owner.Name());

			final Area parentArea = ((Room) affected).getArea();
			instA.setClimateType(parentArea.getClimateType());
			instA.setAtmosphere(parentArea.getAtmosphere());
			instA.setTheme(parentArea.getTheme());
			if(parentArea.getTimeObj() != null)
				instA.setTimeObj((TimeClock) parentArea.getTimeObj().copyOf());

			final List<String> pKeyList = CMLib.database().DBReadPlayerDataKeys(owner.Name(), "PLAYERINSTANCE");
			for(final String key : pKeyList)
			{
				if(key.startsWith(subAreaName))
				{
					int x = key.indexOf("#");
					if(x > 0)
						instA.addProperRoomnumber(areaName+key.substring(x));
				}
			}
			if(instA.numberOfProperIDedRooms() == 0)
			{
				final Room firstR = CMLib.database().DBReadRoomObject(hostRoomID, true, false);
				firstR.delEffect(firstR.fetchEffect(ID()));
				CMLib.database().DBReadContent(hostRoomID, firstR, true);
				firstR.setRoomID(areaName+"#0");
				firstR.setArea(instA);
				CMLib.database().DBCreatePlayerData(owner.Name(), "PLAYERINSTANCE", subAreaName+"#0", 
						CMLib.coffeeMaker().getRoomXML(firstR, null, null, true,false,false));
			}
			CMLib.map().addArea(instA);
			instA.setAreaState(Area.State.ACTIVE);
			final String instanceRoomID = instA.Name() + "#0";
			final Room instanceRoom = instA.getRoom(instanceRoomID);
			if(instanceRoom != null)
				msg.setTarget(instanceRoom);
		}
		return super.okMessage(myHost, msg);
	}
}
