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
 Copyright 2018-2025 Bo Zimmerman

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
public class Beaver extends StdRace
{
	@Override
	public String ID()
	{
		return "Beaver";
	}

	private final static String	localizedStaticName	= CMLib.lang().L("Rodent");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 12;
	}

	@Override
	public int shortestFemale()
	{
		return 12;
	}

	@Override
	public int heightVariance()
	{
		return 8;
	}

	@Override
	public int lightestWeight()
	{
		return 20;
	}

	@Override
	public int weightVariance()
	{
		return 10;
	}

	@Override
	public long forbiddenWornBits()
	{
		return ~(Wearable.WORN_HEAD | Wearable.WORN_EARS | Wearable.WORN_EYES);
	}

	private final static String	localizedStaticRacialCat	= CMLib.lang().L("Rodent");

	@Override
	public String racialCategory()
	{
		return localizedStaticRacialCat;
	}

	private final String[]	racialAbilityNames			= { "RodentSpeak", "Chopping", "Skill_Swim" };
	private final int[]		racialAbilityLevels			= { 1, 9, 3 };
	private final int[]		racialAbilityProficiencies	= { 100, 50, 75 };
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

	// an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[]	parts	= { 0, 2, 2, 1, 1, 0, 0, 1, 4, 4, 1, 0, 1, 1, 1, 0 };

	@Override
	public int[] bodyMask()
	{
		return parts;
	}

	private final int[]	agingChart	= { 0, 1, 2, 3, 4, 4, 5, 5, 6 };

	@Override
	public int[] getAgingChart()
	{
		return agingChart;
	}

	private static Vector<RawMaterial>	resources	= new Vector<RawMaterial>();

	@Override
	public int availabilityCode()
	{
		return Area.THEME_ALLTHEMES | Area.THEME_SKILLONLYMASK;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask() | PhyStats.CAN_SEE_DARK);
	}

	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setRacialStat(CharStats.STAT_STRENGTH, 7);
		affectableStats.setRacialStat(CharStats.STAT_INTELLIGENCE, 1);
		affectableStats.setRacialStat(CharStats.STAT_DEXTERITY, 7);
	}

	@Override
	public void unaffectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.unaffectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STAT_STRENGTH,affectedMOB.baseCharStats().getStat(CharStats.STAT_STRENGTH));
		affectableStats.setStat(CharStats.STAT_MAX_STRENGTH_ADJ,affectedMOB.baseCharStats().getStat(CharStats.STAT_MAX_STRENGTH_ADJ));
		affectableStats.setStat(CharStats.STAT_INTELLIGENCE,affectedMOB.baseCharStats().getStat(CharStats.STAT_INTELLIGENCE));
		affectableStats.setStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ,affectedMOB.baseCharStats().getStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ));
		affectableStats.setStat(CharStats.STAT_DEXTERITY,affectedMOB.baseCharStats().getStat(CharStats.STAT_DEXTERITY));
		affectableStats.setStat(CharStats.STAT_MAX_DEXTERITY_ADJ,affectedMOB.baseCharStats().getStat(CharStats.STAT_MAX_DEXTERITY_ADJ));
	}

	@Override
	public String arriveStr()
	{
		return "scurries in";
	}

	@Override
	public String leaveStr()
	{
		return "scurries";
	}

	@Override
	public Weapon[] getNaturalWeapons()
	{
		if(this.naturalWeaponChoices.length==0)
		{
			final Weapon naturalWeapon = CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName(L("a pair of sharp teeth"));
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
		switch (age)
		{
		case Race.AGE_INFANT:
		case Race.AGE_TODDLER:
			return name().toLowerCase() + " pinkie";
		case Race.AGE_CHILD:
			switch (gender)
			{
			case 'M':
			case 'm':
				return "boy " + name().toLowerCase() + " pup";
			case 'F':
			case 'f':
				return "girl " + name().toLowerCase() + " pup";
			default:
				return "young " + name().toLowerCase();
			}
		default:
			return super.makeMobName(gender, age);
		}
	}

	@Override
	public String healthText(final MOB viewer, final MOB mob)
	{
		final double pct = (CMath.div(mob.curState().getHitPoints(), mob.maxState().getHitPoints()));

		if (pct < .10)
			return L("^r@x1^r is one unhappy critter!^N", mob.name(viewer));
		else
		if (pct < .20)
			return L("^r@x1^r is covered in blood and matted hair.^N", mob.name(viewer));
		else
		if (pct < .30)
			return L("^r@x1^r is bleeding badly from lots of wounds.^N", mob.name(viewer));
		else
		if (pct < .40)
			return L("^y@x1^y has large patches of bloody matted fur.^N", mob.name(viewer));
		else
		if (pct < .50)
			return L("^y@x1^y has some bloody matted fur.^N", mob.name(viewer));
		else
		if (pct < .60)
			return L("^p@x1^p has a lot of cuts and gashes.^N", mob.name(viewer));
		else
		if (pct < .70)
			return L("^p@x1^p has a few cut patches.^N", mob.name(viewer));
		else
		if (pct < .80)
			return L("^g@x1^g has a cut patch of fur.^N", mob.name(viewer));
		else
		if (pct < .90)
			return L("^g@x1^g has some disheveled fur.^N", mob.name(viewer));
		else
		if (pct < .99)
			return L("^g@x1^g has some misplaced hairs.^N", mob.name(viewer));
		else
			return L("^c@x1^c is in perfect health.^N", mob.name(viewer));
	}

	@Override
	public List<RawMaterial> myResources()
	{
		synchronized (resources)
		{
			if (resources.size() == 0)
			{
				resources.addElement(makeResource(L("some @x1 hair", name().toLowerCase()), RawMaterial.RESOURCE_FUR,L("@x1 fur",name().toLowerCase())));
				resources.addElement(makeResource(L("a pair of @x1 teeth", name().toLowerCase()), RawMaterial.RESOURCE_BONE));
				resources.addElement(makeResource(L("some @x1 blood", name().toLowerCase()), RawMaterial.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}
