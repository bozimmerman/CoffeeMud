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
   Copyright 2003-2025 Bo Zimmerman

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
public class Pachyderm extends StdRace
{
	@Override
	public String ID()
	{
		return "Pachyderm";
	}

	private final static String localizedStaticName = CMLib.lang().L("Pachyderm");

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
		return 850;
	}

	@Override
	public int weightVariance()
	{
		return 300;
	}

	@Override
	public long forbiddenWornBits()
	{
		return ~(Wearable.WORN_HEAD | Wearable.WORN_EARS | Wearable.WORN_EYES);
	}

	private final static String localizedStaticRacialCat = CMLib.lang().L("Pachyderm");

	@Override
	public String racialCategory()
	{
		return localizedStaticRacialCat;
	}

	@Override
	public boolean useRideClass()
	{
		return true;
	}

	private final String[]	racialAbilityNames			= { "ElephantSpeak", "Skill_Trample" };
	private final int[]		racialAbilityLevels			= { 1, 10 };
	private final int[]		racialAbilityProficiencies	= { 100, 50 };
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
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,0 ,0 ,1 ,4 ,4 ,1 ,0 ,1 ,1 ,1 ,0 };

	@Override
	public int[] bodyMask()
	{
		return parts;
	}

	private final int[]	agingChart	= { 0, 4, 8, 16, 28, 60, 80, 82, 84 };

	@Override
	public int[] getAgingChart()
	{
		return agingChart;
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_ALLTHEMES | Area.THEME_SKILLONLYMASK;
	}

	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setRacialStat(CharStats.STAT_STRENGTH,18);
		affectableStats.setRacialStat(CharStats.STAT_DEXTERITY,3);
		affectableStats.setRacialStat(CharStats.STAT_INTELLIGENCE,1);
	}

	@Override
	public void unaffectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.unaffectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STAT_STRENGTH,affectedMOB.baseCharStats().getStat(CharStats.STAT_STRENGTH));
		affectableStats.setStat(CharStats.STAT_MAX_STRENGTH_ADJ,affectedMOB.baseCharStats().getStat(CharStats.STAT_MAX_STRENGTH_ADJ));
		affectableStats.setStat(CharStats.STAT_DEXTERITY,affectedMOB.baseCharStats().getStat(CharStats.STAT_DEXTERITY));
		affectableStats.setStat(CharStats.STAT_MAX_DEXTERITY_ADJ,affectedMOB.baseCharStats().getStat(CharStats.STAT_MAX_DEXTERITY_ADJ));
		affectableStats.setStat(CharStats.STAT_INTELLIGENCE,affectedMOB.baseCharStats().getStat(CharStats.STAT_INTELLIGENCE));
		affectableStats.setStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ,affectedMOB.baseCharStats().getStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ));
	}

	@Override
	public Weapon[] getNaturalWeapons()
	{
		if(this.naturalWeaponChoices.length==0)
		{
			final Weapon naturalWeapon=CMClass.getWeapon("GenWeapon");
			naturalWeapon.setName(L("a pair of tusks"));
			naturalWeapon.setMaterial(RawMaterial.RESOURCE_BONE);
			naturalWeapon.setUsesRemaining(1000);
			naturalWeapon.setWeaponDamageType(Weapon.TYPE_PIERCING);
			this.naturalWeaponChoices = new Weapon[] { naturalWeapon };
		}
		return super.getNaturalWeapons();
	}

	@Override
	public String makeMobName(final char gender, final int age)
	{
		switch(age)
		{
			case Race.AGE_INFANT:
			case Race.AGE_TODDLER:
			case Race.AGE_CHILD:
				return name().toLowerCase()+" calf";
			case Race.AGE_YOUNGADULT:
				switch(gender)
				{
				case 'M':
				case 'm':
					return "young " + name().toLowerCase() + " bull";
				case 'F':
				case 'f':
					return "young " + name().toLowerCase() + " cow";
				default:
					return name().toLowerCase();
				}
			case Race.AGE_MATURE:
			case Race.AGE_MIDDLEAGED:
			default:
				switch(gender)
				{
				case 'M':
				case 'm':
					return name().toLowerCase() + " bull";
				case 'F':
				case 'f':
					return name().toLowerCase() + " cow";
				default:
					return name().toLowerCase();
				}
			case Race.AGE_OLD:
			case Race.AGE_VENERABLE:
			case Race.AGE_ANCIENT:
				switch(gender)
				{
				case 'M':
				case 'm':
					return "old " + name().toLowerCase() + " bull";
				case 'F':
				case 'f':
					return "old " + name().toLowerCase() + " cow";
				default:
					return "old " + name().toLowerCase();
				}
		}
	}

	@Override
	public String healthText(final MOB viewer, final MOB mob)
	{
		final double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return L("^r@x1^r is almost dead!^N",mob.name(viewer));
		else
		if(pct<.20)
			return L("^r@x1^r is covered in blood.^N",mob.name(viewer));
		else
		if(pct<.30)
			return L("^r@x1^r is bleeding badly from lots of wounds.^N",mob.name(viewer));
		else
		if(pct<.40)
			return L("^y@x1^y has numerous bloody wounds and gashes.^N",mob.name(viewer));
		else
		if(pct<.50)
			return L("^y@x1^y has some bloody wounds and gashes.^N",mob.name(viewer));
		else
		if(pct<.60)
			return L("^p@x1^p has a few bloody wounds.^N",mob.name(viewer));
		else
		if(pct<.70)
			return L("^p@x1^p is cut and bruised heavily.^N",mob.name(viewer));
		else
		if(pct<.80)
			return L("^g@x1^g has some minor cuts and bruises.^N",mob.name(viewer));
		else
		if(pct<.90)
			return L("^g@x1^g has a few bruises and scratched parts.^N",mob.name(viewer));
		else
		if(pct<.99)
			return L("^g@x1^g has a few small bruises.^N",mob.name(viewer));
		else
			return L("^c@x1^c is in perfect health.^N",mob.name(viewer));
	}

	private static Vector<RawMaterial>	resources	= new Vector<RawMaterial>();

	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				for(int i=0;i<12;i++)
				{
					resources.addElement(makeResource
					(L("a strip of @x1 leather",name().toLowerCase()),RawMaterial.RESOURCE_LEATHER));
				}
				for(int i=0;i<52;i++)
				{
					resources.addElement(makeResource
					(L("a pound of @x1 meat",name().toLowerCase()),RawMaterial.RESOURCE_BEEF));
				}
				resources.addElement(makeResource
				(L("some @x1 blood",name().toLowerCase()),RawMaterial.RESOURCE_BLOOD));
				resources.addElement(makeResource
				(L("a @x1 tusk",name().toLowerCase()),RawMaterial.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
