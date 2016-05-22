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
   Copyright 2016-2016 Bo Zimmerman

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
public class MagmaMephit extends StdRace
{
	@Override
	public String ID()
	{
		return "MagmaMephit";
	}

	public MagmaMephit()
	{
		super();
	}
	
	private final static String localizedStaticName = CMLib.lang().L("Magma Mephit");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 48;
	}

	@Override
	public int shortestFemale()
	{
		return 48;
	}

	@Override
	public int heightVariance()
	{
		return 0;
	}

	@Override
	public int lightestWeight()
	{
		return 60;
	}

	@Override
	public int weightVariance()
	{
		return 0;
	}

	@Override
	public long forbiddenWornBits()
	{
		return 0;
	}

	private final static String localizedStaticRacialCat = CMLib.lang().L("Mephit");

	@Override
	public String racialCategory()
	{
		return localizedStaticRacialCat;
	}

	@Override
	public boolean fertile()
	{
		return false;
	}

	@Override
	public boolean uncharmable()
	{
		return false;
	}

	@Override
	protected boolean destroyBodyAfterUse()
	{
		return false;
	}

	@Override
	public int[] getBreathables()
	{
		return breatheAnythingArray;
	}

	protected static Vector<RawMaterial>	resources	= new Vector<RawMaterial>();

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY | Area.THEME_SKILLONLYMASK;
	}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,0 ,0 };

	@Override
	public int[] bodyMask()
	{
		return parts;
	}

	private final int[]	agingChart	= { 0, 0, 0, 0, 0, YEARS_AGE_LIVES_FOREVER, YEARS_AGE_LIVES_FOREVER, YEARS_AGE_LIVES_FOREVER, YEARS_AGE_LIVES_FOREVER };

	@Override
	public int[] getAgingChart()
	{
		return agingChart;
	}

	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STAT_SAVE_ACID,affectableStats.getStat(CharStats.STAT_SAVE_ACID)+50);
		affectableStats.setStat(CharStats.STAT_SAVE_COLD,affectableStats.getStat(CharStats.STAT_SAVE_COLD)-50);
		affectableStats.setStat(CharStats.STAT_SAVE_ELECTRIC,affectableStats.getStat(CharStats.STAT_SAVE_ELECTRIC)-50);
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
	}

	private final String[]	racialAbilityNames			= { "Dragonbreath"};
	private final int[]		racialAbilityLevels			= { 1, };
	private final int[]		racialAbilityProficiencies	= { 100 };
	private final boolean[]	racialAbilityQuals			= { true };
	private final String[]	racialAbilityParms			= { "" };

	@Override
	public String[] racialAbilityNames()
	{
		return racialAbilityNames;
	}

	@Override
	public int[] racialAbilityLevels()
	{
		return racialAbilityLevels;
	}

	@Override
	public int[] racialAbilityProficiencies()
	{
		return racialAbilityProficiencies;
	}

	@Override
	public boolean[] racialAbilityQuals()
	{
		return racialAbilityQuals;
	}

	@Override
	public String[] racialAbilityParms()
	{
		return racialAbilityParms;
	}

	@Override
	public String makeMobName(char gender, int age)
	{
		return makeMobName('N',Race.AGE_MATURE);
	}

	@Override
	public String healthText(MOB viewer, MOB mob)
	{
		final double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return L("^r@x1^r is almost put out!^N",mob.name(viewer));
		else
		if(pct<.20)
			return L("^r@x1^r is flickering alot and is almost smoked out.^N",mob.name(viewer));
		else
		if(pct<.30)
			return L("^r@x1^r is flickering alot and smoking massively.^N",mob.name(viewer));
		else
		if(pct<.40)
			return L("^y@x1^y is flickering alot and smoking a lot.^N",mob.name(viewer));
		else
		if(pct<.50)
			return L("^y@x1^y is flickering and smoking.^N",mob.name(viewer));
		else
		if(pct<.60)
			return L("^p@x1^p is flickering and smoking somewhat.^N",mob.name(viewer));
		else
		if(pct<.70)
			return L("^p@x1^p is showing large flickers.^N",mob.name(viewer));
		else
		if(pct<.80)
			return L("^g@x1^g is showing some flickers.^N",mob.name(viewer));
		else
		if(pct<.90)
			return L("^g@x1^g is showing small flickers.^N",mob.name(viewer));
		else
		if(pct<.99)
			return L("^g@x1^g is no longer in perfect condition.^N",mob.name(viewer));
		else
			return L("^c@x1^c is in perfect condition.^N",mob.name(viewer));
	}
	
	@Override 
	public DeadBody getCorpseContainer(MOB mob, Room room)
	{
		final DeadBody body = super.getCorpseContainer(mob, room);
		if(body != null)
		{
			body.setMaterial(RawMaterial.RESOURCE_STONE);
		}
		return body;
	}
	
	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
					(L("a pound of stone"),RawMaterial.RESOURCE_STONE));
			}
		}
		return resources;
	}
}
