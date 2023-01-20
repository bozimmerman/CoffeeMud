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
   Copyright 2023-2023 Bo Zimmerman

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
public class Mustelid extends StdRace
{
	@Override
	public String ID()
	{
		return "Mustelid";
	}

	private final static String localizedStaticName = CMLib.lang().L("Mustelid");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 5;
	}

	@Override
	public int shortestFemale()
	{
		return 5;
	}

	@Override
	public int heightVariance()
	{
		return 2;
	}

	@Override
	public int lightestWeight()
	{
		return 15;
	}

	@Override
	public int weightVariance()
	{
		return 15;
	}

	@Override
	public long forbiddenWornBits()
	{
		return Wearable.WORN_HEAD | Wearable.WORN_FEET | Wearable.WORN_TORSO
				| Wearable.WORN_LEGS ;
	}

	private final static String localizedStaticRacialCat = CMLib.lang().L("Mustelid");

	@Override
	public String racialCategory()
	{
		return localizedStaticRacialCat;
	}

	@Override
	public String arriveStr()
	{
		return "shuffles in";
	}

	@Override
	public String leaveStr()
	{
		return "shuffles";
	}

	@Override
	public boolean useRideClass()
	{
		return true;
	}

	private final String[]	racialAbilityNames			= { "AnimalSpeak", "Skill_BurrowHide", "Skill_Burrow" };
	private final int[]		racialAbilityLevels			= { 1, 3, 5 };
	private final int[]		racialAbilityProficiencies	= { 100, 100, 100 };
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

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,0 ,0 ,1 ,4 ,4 ,1 ,0 ,1 ,1 ,1 ,0 };

	@Override
	public int[] bodyMask()
	{
		return parts;
	}

	private final int[]	agingChart	= { 0, 1, 2, 4, 7, 15, 20, 21, 22 };

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
		affectableStats.setRacialStat(CharStats.STAT_STRENGTH,8);
		affectableStats.setRacialStat(CharStats.STAT_DEXTERITY,14);
		affectableStats.setRacialStat(CharStats.STAT_INTELLIGENCE,1);
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_INFRARED);
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
	public Weapon myNaturalWeapon()
	{
		if (naturalWeapon == null)
		{
			naturalWeapon = CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName(L("a pair of sharp teeth"));
			naturalWeapon.setMaterial(RawMaterial.RESOURCE_BONE);
			naturalWeapon.setUsesRemaining(1000);
			naturalWeapon.setWeaponDamageType(Weapon.TYPE_PIERCING);
		}
		return naturalWeapon;
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

	protected static Vector<RawMaterial>	resources	= new Vector<RawMaterial>();
	protected List<RawMaterial> privateResources() { return resources; }

	@Override
	public List<RawMaterial> myResources()
	{
		final List<RawMaterial>	resources	= privateResources();
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.add(makeResource
				(L("a @x1 fur",name().toLowerCase()),RawMaterial.RESOURCE_FUR,L("a strip of @x1 fur",name().toLowerCase())));
				resources.add(makeResource
				(L("some mustelid blood",name().toLowerCase()),RawMaterial.RESOURCE_BLOOD));
				resources.add(makeResource
				(L("a pile of @x1 bones",name().toLowerCase()),RawMaterial.RESOURCE_BONE));
				resources.add(makeResource
				(L("some @x1 claws",name().toLowerCase()),RawMaterial.RESOURCE_BONE));
				resources.add(makeResource
				(L("a @x1 tail",name().toLowerCase()),RawMaterial.RESOURCE_MEAT));
				resources.add(makeResource
				(L("a pound of @x1 meat",name().toLowerCase()),RawMaterial.RESOURCE_MEAT));
			}
		}
		return resources;
	}
}
