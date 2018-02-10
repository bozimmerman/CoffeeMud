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
   Copyright 2001-2018 Bo Zimmerman

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
public class Dwarf extends StdRace
{
	@Override
	public String ID()
	{
		return "Dwarf";
	}

	public Dwarf()
	{
		super();
		super.naturalAbilImmunities.add("Disease_Syphilis");
	}
	
	private final static String localizedStaticName = CMLib.lang().L("Dwarf");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 52;
	}

	@Override
	public int shortestFemale()
	{
		return 48;
	}

	@Override
	public int heightVariance()
	{
		return 8;
	}

	@Override
	public int lightestWeight()
	{
		return 150;
	}

	@Override
	public int weightVariance()
	{
		return 100;
	}

	@Override
	public long forbiddenWornBits()
	{
		return 0;
	}

	private final static String localizedStaticRacialCat = CMLib.lang().L("Dwarf");

	@Override
	public String racialCategory()
	{
		return localizedStaticRacialCat;
	}

	private final String[]	culturalAbilityNames			= { "Dwarven", "Mining" };
	private final int[]		culturalAbilityProficiencies	= { 100, 50 };

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

	private final String[]	racialEffectNames			= { "Skill_Stonecunning" };
	private final int[]		racialEffectLevels			= { 1 };
	private final String[]	racialEffectParms			= { "" };
	
	@Override
	protected String[] racialEffectNames()
	{
		return racialEffectNames;
	}

	@Override
	protected int[] racialEffectLevels()
	{
		return racialEffectLevels;
	}

	@Override
	protected String[] racialEffectParms()
	{
		return racialEffectParms;
	}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,0 ,0 };

	@Override
	public int[] bodyMask()
	{
		return parts;
	}

	private final int[]	agingChart	= { 0, 1, 5, 40, 125, 188, 250, 270, 290 };

	@Override
	public int[] getAgingChart()
	{
		return agingChart;
	}

	private static Vector<RawMaterial>	resources	= new Vector<RawMaterial>();

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_INFRARED);
	}

	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STAT_CONSTITUTION,affectableStats.getStat(CharStats.STAT_CONSTITUTION)+1);
		affectableStats.setStat(CharStats.STAT_MAX_CONSTITUTION_ADJ,affectableStats.getStat(CharStats.STAT_MAX_CONSTITUTION_ADJ)+1);
		affectableStats.setStat(CharStats.STAT_CHARISMA,affectableStats.getStat(CharStats.STAT_CHARISMA)-1);
		affectableStats.setStat(CharStats.STAT_MAX_CHARISMA_ADJ,affectableStats.getStat(CharStats.STAT_MAX_CHARISMA_ADJ)-1);
		affectableStats.setStat(CharStats.STAT_SAVE_POISON,affectableStats.getStat(CharStats.STAT_SAVE_POISON)+10);
	}

	@Override
	public List<Item> outfit(MOB myChar)
	{
		if(outfitChoices==null)
		{
			// Have to, since it requires use of special constructor
			final Armor s1=CMClass.getArmor("GenShirt");
			if(s1 == null)
				return new Vector<Item>();
			outfitChoices=new Vector<Item>();
			s1.setName(L("a grey work tunic"));
			s1.setDisplayText(L("a grey work tunic has been left here."));
			s1.setDescription(L("There are lots of little loops and folks for hanging tools about it."));
			s1.text();
			outfitChoices.add(s1);

			final Armor s2=CMClass.getArmor("GenShoes");
			s2.setName(L("a pair of hefty work boots"));
			s2.setDisplayText(L("some hefty work boots have been left here."));
			s2.setDescription(L("Thick and well worn boots with very tough souls."));
			s2.text();
			outfitChoices.add(s2);

			final Armor p1=CMClass.getArmor("GenPants");
			p1.setName(L("some hefty work pants"));
			p1.setDisplayText(L("some hefty work pants have been left here."));
			p1.setDescription(L("There are lots of little loops and folks for hanging tools about it."));
			p1.text();
			outfitChoices.add(p1);

			final Armor s3=CMClass.getArmor("GenBelt");
			outfitChoices.add(s3);
		}
		return outfitChoices;
	}

	@Override
	public Weapon myNaturalWeapon()
	{
		return funHumanoidWeapon();
	}

	@Override
	public String healthText(MOB viewer, MOB mob)
	{
		final double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return L("^r@x1^r is nearly dead!^N",mob.name(viewer));
		else
		if(pct<.20)
			return L("^r@x1^r is covered in blood.^N",mob.name(viewer));
		else
		if(pct<.30)
			return L("^r@x1^r is bleeding from cuts and gashes.^N",mob.name(viewer));
		else
		if(pct<.40)
			return L("^y@x1^y has numerous wounds.^N",mob.name(viewer));
		else
		if(pct<.50)
			return L("^y@x1^y has some wounds.^N",mob.name(viewer));
		else
		if(pct<.60)
			return L("^p@x1^p has a few cuts.^N",mob.name(viewer));
		else
		if(pct<.70)
			return L("^p@x1^p is cut.^N",mob.name(viewer));
		else
		if(pct<.80)
			return L("^g@x1^g has some bruises.^N",mob.name(viewer));
		else
		if(pct<.90)
			return L("^g@x1^g is very winded.^N",mob.name(viewer));
		else
		if(pct<.99)
			return L("^g@x1^g is slightly winded.^N",mob.name(viewer));
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
				(L("a @x1 beard",name().toLowerCase()),RawMaterial.RESOURCE_FUR));
				resources.addElement(makeResource
				(L("some @x1 blood",name().toLowerCase()),RawMaterial.RESOURCE_BLOOD));
				resources.addElement(makeResource
				(L("a pile of @x1 bones",name().toLowerCase()),RawMaterial.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
