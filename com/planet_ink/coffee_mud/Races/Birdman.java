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
   Copyright 2022-2024 Bo Zimmerman

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
public class Birdman extends StdRace
{
	@Override
	public String ID()
	{
		return "Birdman";
	}

	public Birdman()
	{
		super();
		super.naturalAbilImmunities.add("Disease_Lycanthropy");
	}

	private final static String localizedStaticName = CMLib.lang().L("Birdman");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 53;
	}

	@Override
	public int shortestFemale()
	{
		return 55;
	}

	@Override
	public int heightVariance()
	{
		return 5;
	}

	@Override
	public int lightestWeight()
	{
		return 70;
	}

	@Override
	public int weightVariance()
	{
		return 16;
	}

	@Override
	public String arriveStr()
	{
		return "wanders in";
	}

	@Override
	public String leaveStr()
	{
		return "meanders";
	}

	private final static String localizedStaticRacialCat = CMLib.lang().L("Avian");

	@Override
	public String racialCategory()
	{
		return localizedStaticRacialCat;
	}

	@Override
	public long forbiddenWornBits()
	{
		return Wearable.WORN_ABOUT_BODY | Wearable.WORN_FEET | Wearable.WORN_BACK;
	}

	private final int[]	agingChart	= { 0, 1, 3, 15, 35, 53, 70, 74, 78 };

	@Override
	public int[] getAgingChart()
	{
		return agingChart;
	}

	private final String[]	racialAbilityNames			= { "WingFlying"};
	private final int[]		racialAbilityLevels			= { 5 };
	private final int[]		racialAbilityProficiencies	= { 100 };
	private final boolean[]	racialAbilityQuals			= { false };
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

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,0 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,1 ,2 };

	@Override
	public int[] bodyMask()
	{
		return parts;
	}

	protected static Vector<RawMaterial> resources=new Vector<RawMaterial>();

	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		affectableStats.adjStat(CharStats.STAT_DEXTERITY,1);
		affectableStats.adjStat(CharStats.STAT_STRENGTH,-1);
		affectableStats.adjStat(CharStats.STAT_CHARISMA,1);
	}

	@Override
	public void unaffectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		affectableStats.adjStat(CharStats.STAT_DEXTERITY,-1);
		affectableStats.adjStat(CharStats.STAT_STRENGTH,+1);
		affectableStats.adjStat(CharStats.STAT_CHARISMA,-1);
	}

	@Override
	public Weapon[] getNaturalWeapons()
	{
		if(this.naturalWeaponChoices.length==0)
		{
			final Weapon naturalWeapon=CMClass.getWeapon("GenWeapon");
			naturalWeapon.setName(L("some sharp talons"));
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
				return name().toLowerCase()+" chick";
			case Race.AGE_CHILD:
				return "young "+name().toLowerCase();
			default :
				return super.makeMobName(gender, age);
		}
	}

	@Override
	public String healthText(final MOB viewer, final MOB mob)
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
			return L("^g@x1^g has some ruffled feathers.^N",mob.name(viewer));
		else
			return L("^c@x1^c is in perfect health.^N",mob.name(viewer));
	}

	@Override
	public String getStatAdjDesc()
	{
		return super.getStatAdjDesc();
	}

	@Override
	public List<Item> outfit(final MOB myChar)
	{
		if(outfitChoices==null)
		{
			// Have to, since it requires use of special constructor
			final Armor s1=CMClass.getArmor("GenShirt");
			if(s1 == null)
				return new Vector<Item>();
			outfitChoices=new Vector<Item>();
			outfitChoices.add(s1);
			final Armor p1=CMClass.getArmor("GenPants");
			outfitChoices.add(p1);
			final Armor s3=CMClass.getArmor("GenBelt");
			outfitChoices.add(s3);
			final Armor s4=CMClass.getArmor("GenHat");
			s4.setMaterial(RawMaterial.RESOURCE_IRON);
			s4.setName(L("a crash helmet"));
			s4.setDisplayText(L("a crash helmet has been left here"));
			outfitChoices.add(s4);
			cleanOutfit(outfitChoices);
		}
		return outfitChoices;
	}

	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				(L("some @x1 talons",name().toLowerCase()),RawMaterial.RESOURCE_BONE));
				for(int i=0;i<2;i++)
				{
					resources.addElement(makeResource
					(L("some @x1 feathers",name().toLowerCase()),RawMaterial.RESOURCE_FEATHERS,L("@x1 FEATHER",name().toUpperCase())));
				}
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
