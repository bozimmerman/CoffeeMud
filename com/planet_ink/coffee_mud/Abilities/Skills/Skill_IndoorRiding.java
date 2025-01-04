package com.planet_ink.coffee_mud.Abilities.Skills;
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

/*
   Copyright 2023-2025 Bo Zimmerman

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
public class Skill_IndoorRiding extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_IndoorRiding";
	}

	private final static String localizedName = CMLib.lang().L("Indoor Riding");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_ANIMALAFFINITY;
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		super.executeMsg(host,msg);
		if((msg.source() == affected)
		&&(msg.source().riding() instanceof MOB)
		&&(msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.source().riding().rideBasis()==Rideable.Basis.LAND_BASED)
		&&(msg.target() instanceof Room)
		&&(CMath.bset(((Room)msg.target()).domainType(),Room.INDOORS))
		&&(msg.source().riding().phyStats().weight()>199))
		{
			((MOB)msg.source().riding()).curState().adjMovement(-(11-super.getXLEVELLevel((MOB)affected)),
					((MOB)msg.source().riding()).maxState());
		}
	}

	@Override
	public boolean okMessage(final Environmental host, final CMMsg msg)
	{
		if(!super.okMessage(host, msg))
			return false;
		if((msg.source() == affected)
		&&(msg.source().riding() instanceof MOB)
		&&(msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.source().riding().rideBasis()==Rideable.Basis.LAND_BASED)
		&&(msg.target() instanceof Room)
		&&(CMath.bset(((Room)msg.target()).domainType(),Room.INDOORS))
		&&(msg.source().riding().phyStats().weight()>199))
		{
			msg.source().riding().phyStats().setWeight(100); // simulates squeezing through a door
		}
		return true;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
	}
}
