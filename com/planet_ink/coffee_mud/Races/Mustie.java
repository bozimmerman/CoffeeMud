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
   Copyright 2023-2025 Bo Zimmerman

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
public class Mustie extends StdRace
{
	@Override
	public String ID()
	{
		return "Mustie";
	}

	public Mustie()
	{
		super();
	}

	private final static String localizedStaticName = CMLib.lang().L("Mustie");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 44;
	}

	@Override
	public int shortestFemale()
	{
		return 44;
	}

	@Override
	public int heightVariance()
	{
		return 9;
	}

	@Override
	public int lightestWeight()
	{
		return 65;
	}

	@Override
	public int weightVariance()
	{
		return 15;
	}

	@Override
	public long forbiddenWornBits()
	{
		return 0;
	}

	private final static String localizedStaticRacialCat = CMLib.lang().L("Mustelid");

	@Override
	public String racialCategory()
	{
		return localizedStaticRacialCat;
	}

	private final String[]	culturalAbilityNames			= { "Skill_Burrow", "Foraging", "Searching" };
	private final int[]		culturalAbilityProficiencies	= { 100, 25, 75 };

	@Override
	public String[] culturalAbilityNames()
	{
		return culturalAbilityNames;
	}

	@Override
	public int[] culturalAbilityProficiencies()
	{
		return culturalAbilityProficiencies;
	}

	private final String[]	racialAbilityNames			= { "Skill_Swim" };
	private final int[]		racialAbilityLevels			= { 1 };
	private final int[]		racialAbilityProficiencies	= { 100 };
	private final boolean[]	racialAbilityQuals			= { false };
	private final String[]	racialAbilityParms			= { "" };

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
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,1 ,0 };

	@Override
	public int[] bodyMask()
	{
		return parts;
	}

	private final int[]	agingChart	= { 0, 1, 2, 8, 10, 55, 70, 71, 72 };

	@Override
	public int[] getAgingChart()
	{
		return agingChart;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_DARK);
	}

	protected static Vector<RawMaterial> resources=new Vector<RawMaterial>();

	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.adjStat(CharStats.STAT_CHARISMA,-2);
		affectableStats.adjStat(CharStats.STAT_WISDOM,1);
		affectableStats.adjStat(CharStats.STAT_CONSTITUTION,2);
	}

	@Override
	public void unaffectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.unaffectCharStats(affectedMOB, affectableStats);
		affectableStats.adjStat(CharStats.STAT_CHARISMA,2);
		affectableStats.adjStat(CharStats.STAT_WISDOM,-1);
		affectableStats.adjStat(CharStats.STAT_CONSTITUTION,-2);
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
	public List<Item> outfit(final MOB myChar)
	{
		if(outfitChoices==null)
		{
			outfitChoices = new Vector<Item>();
			final Armor p1=CMClass.getArmor("GenPants");
			p1.setName(L("a loincloth"));
			p1.setDisplayText(L("a simple loincloth sits here."));
			p1.setDescription(L("A simple piece of cloth for wrapping around your mid-parts."));
			p1.setRawProperLocationBitmap(Wearable.WORN_WAIST);
			p1.basePhyStats().setAbility(0);
			((Container)p1).setCapacity(20);
			((Container)p1).setContainTypes(Container.CONTAIN_DAGGERS|Container.CONTAIN_ONEHANDWEAPONS|Container.CONTAIN_SWORDS|Container.CONTAIN_OTHERWEAPONS);
			p1.text();
			outfitChoices.add(p1);
			cleanOutfit(outfitChoices);
		}
		return outfitChoices;
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
				(L("a @x1 tail",name().toLowerCase()),RawMaterial.RESOURCE_MEAT));
				resources.add(makeResource
				(L("a pound of @x1 meat",name().toLowerCase()),RawMaterial.RESOURCE_MEAT));
			}
		}
		return resources;
	}
}
