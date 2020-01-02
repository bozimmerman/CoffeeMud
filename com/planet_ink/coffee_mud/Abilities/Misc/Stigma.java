package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Faction.FData;
import com.planet_ink.coffee_mud.Common.interfaces.Faction.FRange;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2018-2020 Bo Zimmerman

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
public class Stigma extends StdAbility
{
	@Override
	public String ID()
	{
		return "Stigma";
	}

	private final static String	localizedName	= CMLib.lang().L("Stigma");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return L("(Stigma)");
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_RACIALABILITY;
	}

	@Override
	public long flags()
	{
		return super.flags()|Ability.FLAG_NOUNINVOKING;
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
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
	}

	protected SHashtable<String, FData>		factions		= new SHashtable<String, FData>(1);
	protected volatile int checkDown = 0;

	protected int playerFaction(final MOB mob, final Faction F)
	{
		final int playerNum=mob.fetchFaction(F.factionID());
		final int midway = F.middle();
		if(playerNum==midway)
			return midway;
		if(playerNum>midway)
			return F.maximum();
		return F.minimum();
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		if(affected instanceof MOB)
		{
			for (final Enumeration<FData> e = factions.elements(); e.hasMoreElements();)
				e.nextElement().affectPhyStats(affected, affectableStats);
		}
	}

	@Override
	public void affectCharStats(final MOB affectedMob, final CharStats affectableStats)
	{
		for (final Enumeration<FData> e = factions.elements(); e.hasMoreElements();)
			e.nextElement().affectCharStats(affectedMob, affectableStats);
	}

	@Override
	public void affectCharState(final MOB affectedMob, final CharState affectableMaxState)
	{
		for (final Enumeration<Faction.FData> e = factions.elements(); e.hasMoreElements();)
			e.nextElement().affectCharState(affectedMob, affectableMaxState);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			final boolean isMonster=mob.isMonster();
			if((--checkDown)<0)
			{
				checkDown=CMLib.dice().roll(1, isMonster?100:10, 0);
				for(final Enumeration<String> f=mob.factions();f.hasMoreElements();)
				{
					final String factionID=f.nextElement().toUpperCase();
					if(!factions.containsKey(factionID))
					{
						final Faction F=CMLib.factions().getFaction(factionID);
						if(F!=null)
						{
							final int pfac=playerFaction(mob,F);
							final FRange newFR=F.fetchRange(pfac);
							final FRange oldFR=F.fetchRange(mob.fetchFaction(F.factionID()));
							if(newFR != oldFR)
							{
								final FData data = F.makeFactionData(mob);
								factions.put(factionID, data);
								data.setValue(pfac);
							}
						}
					}
				}
				for (final Enumeration<Faction.FData> t = factions.elements(); t.hasMoreElements();)
				{
					final Faction.FData T = t.nextElement();
					final String factionID = T.getFaction().factionID();
					final Faction F = CMLib.factions().getFaction(factionID);
					if ((F != null) && (mob.hasFaction(factionID)))
					{
						if (T.requiresUpdating())
						{
							final int oldValue = T.value();
							F.updateFactionData(mob, T);
							T.setValue(oldValue);
						}
						final int playerNum=playerFaction(mob,F);
						if(T.value() != playerNum)
							T.setValue(playerNum);
					}
					else
						factions.remove(factionID);
				}
			}
			for (final Enumeration<Faction.FData> t = factions.elements(); t.hasMoreElements();)
			{
				final Faction.FData T = t.nextElement();
				T.tick(ticking, tickID);
			}
		}
		return true;
	}

	@Override
	public boolean okMessage(final Environmental host, final CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			for (final Enumeration<Faction.FData> e = factions.elements(); e.hasMoreElements();)
			{
				final Faction.FData fD = e.nextElement();
				if (!fD.getFaction().okMessage(affected, msg))
					return false;
				if (!fD.okMessage(affected, msg))
					return false;
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			for (final Enumeration<Faction.FData> e = factions.elements(); e.hasMoreElements();)
			{
				final Faction.FData fD = e.nextElement();
				fD.getFaction().executeMsg(affected, msg);
				fD.executeMsg(affected, msg);
			}
		}
		super.executeMsg(myHost,msg);
	}
}
