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
   Copyright 2002-2018 Bo Zimmerman

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
public class Raven extends StdRace
{
	@Override
	public String ID()
	{
		return "Raven";
	}

	private final static String localizedStaticName = CMLib.lang().L("Raven");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 6;
	}

	@Override
	public int shortestFemale()
	{
		return 6;
	}

	@Override
	public int heightVariance()
	{
		return 5;
	}

	@Override
	public int lightestWeight()
	{
		return 2;
	}

	@Override
	public int weightVariance()
	{
		return 5;
	}

	@Override
	public long forbiddenWornBits()
	{
		return ~(Wearable.WORN_HEAD | Wearable.WORN_EYES);
	}

	private final static String localizedStaticRacialCat = CMLib.lang().L("Avian");

	@Override
	public String racialCategory()
	{
		return localizedStaticRacialCat;
	}

	private final String[]	racialAbilityNames			= { "WingFlying", "RavenSpeak" };
	private final int[]		racialAbilityLevels			= { 1, 1 };
	private final int[]		racialAbilityProficiencies	= { 100, 100 };
	private final boolean[]	racialAbilityQuals			= { false, false };
	private final String[]	racialAbilityParms			= { "", "" };

	@Override
	protected String[] racialAbilityNames()
	{
		return racialAbilityNames;
	}

	@Override
	protected int[] racialAbilityLevels()
	{
		return racialAbilityLevels;
	}

	@Override
	protected int[] racialAbilityProficiencies()
	{
		return racialAbilityProficiencies;
	}

	@Override
	protected boolean[] racialAbilityQuals()
	{
		return racialAbilityQuals;
	}

	@Override
	public String[] racialAbilityParms()
	{
		return racialAbilityParms;
	}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,0 ,0 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,1 ,2 };

	@Override
	public int[] bodyMask()
	{
		return parts;
	}

	private static Vector<RawMaterial>	resources	= new Vector<RawMaterial>();

	private final int[]						agingChart	= { 0, 1, 2, 4, 7, 15, 20, 21, 22 };

	@Override
	public int[] getAgingChart()
	{
		return agingChart;
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY | Area.THEME_SKILLONLYMASK;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((affectableStats.disposition() & (PhyStats.IS_SITTING|PhyStats.IS_SLEEPING))==0)
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_FLYING);
	}

	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setRacialStat(CharStats.STAT_STRENGTH,8);
		affectableStats.setRacialStat(CharStats.STAT_DEXTERITY,12);
		affectableStats.setRacialStat(CharStats.STAT_INTELLIGENCE,1);
	}

	@Override
	public String arriveStr()
	{
		return "flies in";
	}

	@Override
	public String leaveStr()
	{
		return "flies";
	}

	@Override
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName(L("a menacing beak"));
			naturalWeapon.setMaterial(RawMaterial.RESOURCE_BONE);
			naturalWeapon.setUsesRemaining(1000);
			naturalWeapon.setWeaponDamageType(Weapon.TYPE_NATURAL);
		}
		return naturalWeapon;
	}

	@Override
	public String healthText(MOB viewer, MOB mob)
	{
		final double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return L("^r@x1^r is hovering on deaths door!^N",mob.name(viewer));
		else
		if(pct<.20)
			return L("^r@x1^r is covered in blood and matted feathers.^N",mob.name(viewer));
		else
		if(pct<.30)
			return L("^r@x1^r is bleeding badly from lots of wounds.^N",mob.name(viewer));
		else
		if(pct<.40)
			return L("^y@x1^y has numerous bloody matted feathers.^N",mob.name(viewer));
		else
		if(pct<.50)
			return L("^y@x1^y has some bloody matted feathers.^N",mob.name(viewer));
		else
		if(pct<.60)
			return L("^p@x1^p has a lot of missing feathers.^N",mob.name(viewer));
		else
		if(pct<.70)
			return L("^p@x1^p has a few missing feathers.^N",mob.name(viewer));
		else
		if(pct<.80)
			return L("^g@x1^g has a missing feather.^N",mob.name(viewer));
		else
		if(pct<.90)
			return L("^g@x1^g has a few feathers out of place.^N",mob.name(viewer));
		else
		if(pct<.99)
			return L("^g@x1^g has a some ruffled feathers.^N",mob.name(viewer));
		else
			return L("^c@x1^c is in perfect health.^N",mob.name(viewer));
	}

	@Override
	public String makeMobName(char gender, int age)
	{
		switch(age)
		{
			case Race.AGE_INFANT:
			case Race.AGE_TODDLER:
				return name().toLowerCase()+" chick";
			case Race.AGE_CHILD:
				return "young "+name().toLowerCase();
			default :
				return super.makeMobName(gender, age);
		}
	}

	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				(L("some @x1 feathers",name().toLowerCase()),RawMaterial.RESOURCE_FEATHERS));
				resources.addElement(makeResource
				(L("some @x1 meat",name().toLowerCase()),RawMaterial.RESOURCE_POULTRY));
				resources.addElement(makeResource
				(L("some @x1 blood",name().toLowerCase()),RawMaterial.RESOURCE_BLOOD));
				resources.addElement(makeResource
				(L("a pile of @x1 bones",name().toLowerCase()),RawMaterial.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
