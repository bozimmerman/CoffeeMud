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
   Copyright 2003-2018 Bo Zimmerman

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
public class Toadstool extends StdRace
{
	@Override
	public String ID()
	{
		return "Toadstool";
	}

	private final static String localizedStaticName = CMLib.lang().L("Toadstool");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 1;
	}

	@Override
	public int shortestFemale()
	{
		return 1;
	}

	@Override
	public int heightVariance()
	{
		return 1;
	}

	@Override
	public int lightestWeight()
	{
		return 1;
	}

	@Override
	public int weightVariance()
	{
		return 1;
	}

	@Override
	public long forbiddenWornBits()
	{
		return Integer.MAX_VALUE;
	}

	private final static String localizedStaticRacialCat = CMLib.lang().L("Vegetation");

	@Override
	public String racialCategory()
	{
		return localizedStaticRacialCat;
	}

	@Override
	public int availabilityCode()
	{
		return 0;
	}

	@Override
	public int[] getBreathables()
	{
		return breatheAnythingArray;
	}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,0 ,0 ,0 ,0 ,0 ,0 ,1 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 };

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

	private static Vector<RawMaterial>	resources	= new Vector<RawMaterial>();

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setWeight(1);
		affectableStats.setHeight(1);
		affectableStats.setAttackAdjustment(0);
		affectableStats.setArmor(0);
		affectableStats.setSensesMask(affectableStats.sensesMask()
			|PhyStats.CAN_NOT_MOVE|PhyStats.CAN_NOT_SPEAK|PhyStats.CAN_NOT_TASTE);
		affectableStats.setDamage(0);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_GOLEM);
	}

	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setRacialStat(CharStats.STAT_STRENGTH,1);
		affectableStats.setRacialStat(CharStats.STAT_DEXTERITY,1);
		affectableStats.setRacialStat(CharStats.STAT_INTELLIGENCE,1);
	}

	@Override
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName(L("the toadstool shuffle"));
			naturalWeapon.setMaterial(RawMaterial.RESOURCE_MUSHROOMS);
			naturalWeapon.setUsesRemaining(1000);
			naturalWeapon.setWeaponDamageType(Weapon.TYPE_NATURAL);
		}
		return naturalWeapon;
	}

	@Override
	public String makeMobName(char gender, int age)
	{
		return super.makeMobName('N', Race.AGE_MATURE);
	}

	@Override
	public String healthText(MOB viewer, MOB mob)
	{
		final double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return L("^r@x1^r is almost squashed!^N",mob.name(viewer));
		else
		if(pct<.25)
			return L("^y@x1^y is severely gashed and bruised.^N",mob.name(viewer));
		else
		if(pct<.40)
			return L("^p@x1^p has lots of gashes and bruises.^N",mob.name(viewer));
		else
		if(pct<.55)
			return L("^p@x1^p has some serious bruises.^N",mob.name(viewer));
		else
		if(pct<.70)
			return L("^g@x1^g has some bruises.^N",mob.name(viewer));
		else
		if(pct<.85)
			return L("^g@x1^g has a few small bruises.^N",mob.name(viewer));
		else
		if(pct<.95)
			return L("^g@x1^g is barely bruised.^N",mob.name(viewer));
		else
			return L("^c@x1^c is in perfect condition^N",mob.name(viewer));
	}

	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				for(int i=0;i<5;i++)
				{
					resources.addElement(makeResource
					(L("some @x1 flesh",name().toLowerCase()),RawMaterial.RESOURCE_MUSHROOMS));
				}
			}
		}
		return resources;
	}
}
