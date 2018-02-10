package com.planet_ink.coffee_mud.Races;
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
   Copyright 2016-2018 Bo Zimmerman

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
public class SmokeMephit extends Mephit
{
	@Override
	public String ID()
	{
		return "SmokeMephit";
	}

	public SmokeMephit()
	{
		super();
	}
	
	private final static String localizedStaticName = CMLib.lang().L("Smoke Mephit");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STAT_SAVE_PIERCE, affectableStats.getStat(CharStats.STAT_SAVE_PIERCE)+50);
		affectableStats.setStat(CharStats.STAT_SAVE_SLASH, affectableStats.getStat(CharStats.STAT_SAVE_SLASH)+50);
		affectableStats.setStat(CharStats.STAT_SAVE_FIRE, affectableStats.getStat(CharStats.STAT_SAVE_FIRE)+50);
	}

	@Override
	public int lightestWeight()
	{
		return 1;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
	}

	private final String[]	racialAbilityNames			= { "Dragonbreath"};
	private final int[]		racialAbilityLevels			= { 1, };
	private final int[]		racialAbilityProficiencies	= { 100 };
	private final boolean[]	racialAbilityQuals			= { false };
	private final String[]	racialAbilityParms			= { "dust lesser" };

	@Override
	public String[] racialAbilityNames()
	{
		return CMParms.combine(super.racialAbilityNames(), racialAbilityNames);
	}

	@Override
	public int[] racialAbilityLevels()
	{
		return CMParms.combine(super.racialAbilityLevels(), racialAbilityLevels);
	}

	@Override
	public int[] racialAbilityProficiencies()
	{
		return CMParms.combine(super.racialAbilityProficiencies(),  racialAbilityProficiencies);
	}

	@Override
	public boolean[] racialAbilityQuals()
	{
		return CMParms.combine(super.racialAbilityQuals(),  racialAbilityQuals);
	}

	@Override
	public String[] racialAbilityParms()
	{
		return CMParms.combine(super.racialAbilityParms(),  racialAbilityParms);
	}

	@Override
	public String healthText(MOB viewer, MOB mob)
	{
		final double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return L("^r@x1^r is unstable and almost disintegrated!^N",mob.name(viewer));
		else
		if(pct<.20)
			return L("^r@x1^r is nearing disintegration.^N",mob.name(viewer));
		else
		if(pct<.30)
			return L("^r@x1^r is noticeably disintegrating.^N",mob.name(viewer));
		else
		if(pct<.40)
			return L("^y@x1^y is very damaged and slightly disintegrated.^N",mob.name(viewer));
		else
		if(pct<.50)
			return L("^y@x1^y is very damaged.^N",mob.name(viewer));
		else
		if(pct<.60)
			return L("^p@x1^p is starting to show major damage.^N",mob.name(viewer));
		else
		if(pct<.70)
			return L("^p@x1^p is definitely damaged.^N",mob.name(viewer));
		else
		if(pct<.80)
			return L("^g@x1^g is disheveled and mildly damaged.^N",mob.name(viewer));
		else
		if(pct<.90)
			return L("^g@x1^g is noticeably disheveled.^N",mob.name(viewer));
		else
		if(pct<.99)
			return L("^g@x1^g is slightly disheveled.^N",mob.name(viewer));
		else
			return L("^c@x1^c is in perfect condition.^N",mob.name(viewer));
	}

	@Override 
	public DeadBody getCorpseContainer(MOB mob, Room room)
	{
		final DeadBody body = super.getCorpseContainer(mob, room);
		if(body != null)
		{
			body.setMaterial(RawMaterial.RESOURCE_DUST);
		}
		return body;
	}
	
	private static Vector<RawMaterial>	resources	= new Vector<RawMaterial>();

	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
					(L("a pound of dust"),RawMaterial.RESOURCE_DUST));
			}
		}
		return resources;
	}
}
