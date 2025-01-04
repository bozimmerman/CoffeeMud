package com.planet_ink.coffee_mud.MOBS;
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

import java.io.IOException;
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
public class CombatDummy extends StdMOB
{
	@Override
	public String ID()
	{
		return "CombatDummy";
	}

	public CombatDummy()
	{
		super();

		_name="a combat dummy";
		setDescription("The combat dummy is ready to feel the pain.");
		setDisplayText("A combat dummy is standing here.");
		CMLib.factions().setAlignment(this,Faction.Align.NEUTRAL);
		setMoney(0);
		setWimpHitPoint(0);

		basePhyStats().setDamage(0);

		baseCharStats().setMyRace(CMClass.getRace("WoodGolem"));
		baseCharStats().getMyRace().startRacing(this,false);

		basePhyStats().setAbility(999);
		basePhyStats().setLevel(4);
		basePhyStats().setArmor(80);
		basePhyStats().setSensesMask(basePhyStats().sensesMask()|PhyStats.CAN_NOT_MOVE|PhyStats.CAN_NOT_SPEAK|PhyStats.CAN_NOT_THINK);

		recoverMaxState();
		resetToMaxState();
		recoverPhyStats();
		recoverCharStats();
	}

	protected int lastLevel = -1;

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if(lastLevel != phyStats().level())
		{
			lastLevel = phyStats().level();
			basePhyStats().setArmor(CMLib.leveler().getLevelMOBArmor(this));
			phyStats().setArmor(basePhyStats().armor());
		}
		curState().setHitPoints(maxState().getHitPoints());
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.sourceMinor()==CMMsg.TYP_COMMANDFAIL)
		&&(msg.targetMessage()!=null)
		&&(msg.targetMessage().length()>0))
		{
			if(Character.toUpperCase(msg.targetMessage().charAt(0)) == 'G')
			{
				final List<String> parsedFail = CMParms.parse(msg.targetMessage());
				if(parsedFail.size()<2)
					return true;
				final String cmd=parsedFail.get(0).toUpperCase();
				if(!("GET".startsWith(cmd)))
					return true;
				final Room R=msg.source().location();
				if(R.fetchInhabitant(CMParms.combine(parsedFail,1))!=this)
					return true;
				final CagedAnimal cI=(CagedAnimal)CMClass.getItem("GenCaged");
				cI.cageMe(this);
				cI.setCageFlagsBitmap(CagedAnimal.CAGEFLAG_TO_MOB_PROGRAMMATICALLY);
				R.addItem(cI, Expire.Player_Drop);
				CMLib.commands().postGet(msg.source(), null, cI, false);
				cI.setCageFlagsBitmap(0);
				return false;
			}
		}
		if(msg.amISource(this)
		&&(msg.sourceMinor()==CMMsg.TYP_DEATH))
		{
			if(fetchAbility("Dueling")!=null)
				return super.okMessage(myHost,msg);
			resetToMaxState();
			return false;
		}
		return super.okMessage(myHost, msg);
	}
}
