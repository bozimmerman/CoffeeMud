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
public class Equine extends StdRace
{
	@Override
	public String ID()
	{
		return "Equine";
	}

	private final static String localizedStaticName = CMLib.lang().L("Equine");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 60;
	}

	@Override
	public int shortestFemale()
	{
		return 60;
	}

	@Override
	public int heightVariance()
	{
		return 12;
	}

	@Override
	public int lightestWeight()
	{
		return 1250;
	}

	@Override
	public int weightVariance()
	{
		return 400;
	}

	@Override
	public long forbiddenWornBits()
	{
		return ~(Wearable.WORN_HEAD | Wearable.WORN_EARS | Wearable.WORN_EYES | Wearable.WORN_NECK);
	}

	private final static String localizedStaticRacialCat = CMLib.lang().L("Equine");

	@Override
	public String racialCategory()
	{
		return localizedStaticRacialCat;
	}

	private final String[]	racialAbilityNames			= { "Fighter_Kick", "HorseSpeak", "Skill_Buck" };
	private final int[]		racialAbilityLevels			= { 5, 1, 5 };
	private final int[]		racialAbilityProficiencies	= { 40, 100, 50 };
	private final boolean[]	racialAbilityQuals			= { false, false, false };
	private final String[]	racialAbilityParms			= { "", "", "" };

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

	@Override
	public boolean useRideClass()
	{
		return true;
	}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,0 ,0 ,1 ,4 ,4 ,1 ,0 ,1 ,1 ,1 ,0 };

	@Override
	public int[] bodyMask()
	{
		return parts;
	}

	private final int[]	agingChart	= { 0, 1, 2, 6, 9, 18, 24, 28, 32 };

	@Override
	public int[] getAgingChart()
	{
		return agingChart;
	}

	private static Vector<RawMaterial>	resources	= new Vector<RawMaterial>();

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY | Area.THEME_SKILLONLYMASK;
	}

	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setRacialStat(CharStats.STAT_STRENGTH,18);
		affectableStats.setRacialStat(CharStats.STAT_DEXTERITY,6);
		affectableStats.setRacialStat(CharStats.STAT_INTELLIGENCE,1);
	}

	@Override
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName(L("a pair of hooves"));
			naturalWeapon.setMaterial(RawMaterial.RESOURCE_BONE);
			naturalWeapon.setUsesRemaining(1000);
			naturalWeapon.setWeaponDamageType(Weapon.TYPE_BASHING);
		}
		return naturalWeapon;
	}

	@Override
	public String makeMobName(char gender, int age)
	{
		switch(age)
		{
			case Race.AGE_INFANT:
			case Race.AGE_TODDLER:
				return name().toLowerCase()+" foal";
			case Race.AGE_CHILD:
			case Race.AGE_YOUNGADULT:
			{
				switch(gender)
				{
				case 'M':
				case 'm':
					return name().toLowerCase() + " colt";
				case 'F':
				case 'f':
					return name().toLowerCase() + " filly";
				default:
					return "young " + name().toLowerCase();
				}
			}
			case Race.AGE_MATURE:
			case Race.AGE_MIDDLEAGED:
			default:
			{
				switch(gender)
				{
				case 'M':
				case 'm':
					return name().toLowerCase() + " stud";
				case 'F':
				case 'f':
					return name().toLowerCase() + " stallion";
				default:
					return name().toLowerCase();
				}
			}
			case Race.AGE_OLD:
			case Race.AGE_VENERABLE:
			case Race.AGE_ANCIENT:
			{
				switch(gender)
				{
				case 'M':
				case 'm':
					return "old male " + name().toLowerCase();
				case 'F':
				case 'f':
					return "old female " + name().toLowerCase();
				default:
					return "old " + name().toLowerCase();
				}
			}
		}
	}

	@Override
	public String healthText(MOB viewer, MOB mob)
	{
		final double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return L("^r@x1^r is hovering on deaths door!^N",mob.name(viewer));
		else
		if(pct<.20)
			return L("^r@x1^r is covered in blood and matted hair.^N",mob.name(viewer));
		else
		if(pct<.30)
			return L("^r@x1^r is bleeding badly from lots of wounds.^N",mob.name(viewer));
		else
		if(pct<.40)
			return L("^y@x1^y has large patches of bloody matted fur.^N",mob.name(viewer));
		else
		if(pct<.50)
			return L("^y@x1^y has some bloody matted fur.^N",mob.name(viewer));
		else
		if(pct<.60)
			return L("^p@x1^p has a lot of cuts and gashes.^N",mob.name(viewer));
		else
		if(pct<.70)
			return L("^p@x1^p has a few cut patches.^N",mob.name(viewer));
		else
		if(pct<.80)
			return L("^g@x1^g has a cut patch of fur.^N",mob.name(viewer));
		else
		if(pct<.90)
			return L("^g@x1^g has some disheveled fur.^N",mob.name(viewer));
		else
		if(pct<.99)
			return L("^g@x1^g has some misplaced hairs.^N",mob.name(viewer));
		else
			return L("^c@x1^c is in perfect health.^N",mob.name(viewer));
	}

	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				(L("@x1 mane",name().toLowerCase()),RawMaterial.RESOURCE_FUR));
				for(int i=0;i<2;i++)
				{
					resources.addElement(makeResource
					(L("a strip of @x1 leather",name().toLowerCase()),RawMaterial.RESOURCE_LEATHER));
				}
				resources.addElement(makeResource
				(L("a pound of @x1 meat",name().toLowerCase()),RawMaterial.RESOURCE_BEEF));
				resources.addElement(makeResource
				(L("some @x1 blood",name().toLowerCase()),RawMaterial.RESOURCE_BLOOD));
				resources.addElement(makeResource
				(L("a pile of @x1 bones",name().toLowerCase()),RawMaterial.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
