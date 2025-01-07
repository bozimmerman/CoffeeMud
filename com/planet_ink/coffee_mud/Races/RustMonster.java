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
   Copyright 2024-2024 Bo Zimmerman

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
public class RustMonster extends StdRace
{
	@Override
	public String ID()
	{
		return "RustMonster";
	}

	private final static String localizedStaticName = CMLib.lang().L("Rust Monster");

	@Override
	public String name()
	{
		return localizedStaticName;
	}


	public RustMonster()
	{
		super();
		super.naturalAbilImmunities.add("Disease_Lockjaw");
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
		return ~(Wearable.WORN_HEAD | Wearable.WORN_EARS | Wearable.WORN_EYES | Wearable.WORN_NECK| Wearable.WORN_ABOUT_BODY);
	}

	private final static String localizedStaticRacialCat = CMLib.lang().L("Unique");

	@Override
	public String racialCategory()
	{
		return localizedStaticRacialCat;
	}

	private final String[]	racialAbilityNames			= { "Skill_RustingStrike" };
	private final int[]		racialAbilityLevels			= { 1};
	private final int[]		racialAbilityProficiencies	= { 99};
	private final boolean[]	racialAbilityQuals			= { false};
	private final String[]	racialAbilityParms			= { ""};

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
	private static final int[] parts={2 ,2 ,2 ,1 ,0 ,0 ,0 ,1 ,4 ,4 ,0 ,0 ,1 ,1 ,1 ,0 };

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

	@Override
	public int availabilityCode()
	{
		return Area.THEME_ALLTHEMES | Area.THEME_SKILLONLYMASK;
	}

	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setRacialStat(CharStats.STAT_STRENGTH,13);
		affectableStats.setRacialStat(CharStats.STAT_DEXTERITY,12);
		affectableStats.setRacialStat(CharStats.STAT_CONSTITUTION,13);
		affectableStats.setRacialStat(CharStats.STAT_WISDOM,13);
		affectableStats.setRacialStat(CharStats.STAT_CHARISMA,6);
		affectableStats.setRacialStat(CharStats.STAT_INTELLIGENCE,1);
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_DARK);
	}
	@Override
	public void unaffectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.unaffectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STAT_STRENGTH,affectedMOB.baseCharStats().getStat(CharStats.STAT_STRENGTH));
		affectableStats.setStat(CharStats.STAT_MAX_STRENGTH_ADJ,affectedMOB.baseCharStats().getStat(CharStats.STAT_MAX_STRENGTH_ADJ));
		affectableStats.setStat(CharStats.STAT_DEXTERITY,affectedMOB.baseCharStats().getStat(CharStats.STAT_DEXTERITY));
		affectableStats.setStat(CharStats.STAT_MAX_DEXTERITY_ADJ,affectedMOB.baseCharStats().getStat(CharStats.STAT_MAX_DEXTERITY_ADJ));
		affectableStats.setStat(CharStats.STAT_CONSTITUTION,affectedMOB.baseCharStats().getStat(CharStats.STAT_CONSTITUTION));
		affectableStats.setStat(CharStats.STAT_MAX_CONSTITUTION_ADJ,affectedMOB.baseCharStats().getStat(CharStats.STAT_MAX_CONSTITUTION_ADJ));
		affectableStats.setStat(CharStats.STAT_WISDOM,affectedMOB.baseCharStats().getStat(CharStats.STAT_WISDOM));
		affectableStats.setStat(CharStats.STAT_MAX_WISDOM_ADJ,affectedMOB.baseCharStats().getStat(CharStats.STAT_MAX_WISDOM_ADJ));
		affectableStats.setStat(CharStats.STAT_CHARISMA,affectedMOB.baseCharStats().getStat(CharStats.STAT_CHARISMA));
		affectableStats.setStat(CharStats.STAT_MAX_CHARISMA_ADJ,affectedMOB.baseCharStats().getStat(CharStats.STAT_MAX_CHARISMA_ADJ));
		affectableStats.setStat(CharStats.STAT_INTELLIGENCE,affectedMOB.baseCharStats().getStat(CharStats.STAT_INTELLIGENCE));
		affectableStats.setStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ,affectedMOB.baseCharStats().getStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ));
	}

	@Override
	public Weapon[] getNaturalWeapons()
	{
		if(this.naturalWeaponChoices.length==0)
		{
			final Weapon naturalWeapon=CMClass.getWeapon("GenWeapon");
			naturalWeapon.setName(L("a bite"));
			naturalWeapon.setMaterial(RawMaterial.RESOURCE_BONE);
			naturalWeapon.setUsesRemaining(1000);
			naturalWeapon.setWeaponDamageType(Weapon.TYPE_BASHING);
			this.naturalWeaponChoices = new Weapon[] { naturalWeapon };
		}
		return super.getNaturalWeapons();
	}

	@Override
	public String healthText(final MOB viewer, final MOB mob)
	{
		final double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return L("^r@x1^r is near destruction!^N",mob.name(viewer));
		else
		if(pct<.20)
			return L("^r@x1^r is massively dented and damaged.^N",mob.name(viewer));
		else
		if(pct<.30)
			return L("^r@x1^r is extremely dented and damaged.^N",mob.name(viewer));
		else
		if(pct<.40)
			return L("^y@x1^y is very dented and damaged.^N",mob.name(viewer));
		else
		if(pct<.50)
			return L("^y@x1^y is dented and damaged.^N",mob.name(viewer));
		else
		if(pct<.60)
			return L("^p@x1^p is dented and slightly damaged.^N",mob.name(viewer));
		else
		if(pct<.70)
			return L("^p@x1^p is showing large rusty dents.^N",mob.name(viewer));
		else
		if(pct<.80)
			return L("^g@x1^g is showing some rusty dents.^N",mob.name(viewer));
		else
		if(pct<.90)
			return L("^g@x1^g is showing small rusty dents.^N",mob.name(viewer));
		else
		if(pct<.99)
			return L("^g@x1^g is no longer in perfect condition.^N",mob.name(viewer));
		else
			return L("^c@x1^c is in perfect condition.^N",mob.name(viewer));
	}
}
