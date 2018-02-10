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
public class Gnome extends StdRace
{
	@Override
	public String ID()
	{
		return "Gnome";
	}

	public Gnome()
	{
		super();
		super.naturalAbilImmunities.add("Disease_Syphilis");
	}
	
	private final static String localizedStaticName = CMLib.lang().L("Gnome");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 40;
	}

	@Override
	public int shortestFemale()
	{
		return 36;
	}

	@Override
	public int heightVariance()
	{
		return 6;
	}

	@Override
	public int lightestWeight()
	{
		return 60;
	}

	@Override
	public int weightVariance()
	{
		return 50;
	}

	@Override
	public long forbiddenWornBits()
	{
		return 0;
	}

	@Override
	public int getXPAdjustment()
	{
		return 5;
	}

	private final static String localizedStaticRacialCat = CMLib.lang().L("Gnome");

	@Override
	public String racialCategory()
	{
		return localizedStaticRacialCat;
	}

	private final String[]	culturalAbilityNames			= { "Gnomish", "Digging", "Skill_BurrowHide" };
	private final int[]		culturalAbilityProficiencies	= { 100, 50, 25 };

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

	private final String[]	racialEffectNames			= { "Chant_Burrowspeak" };
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

	private final int[]	agingChart	= { 0, 1, 5, 40, 100, 150, 200, 230, 260 };

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
		affectableStats.setStat(CharStats.STAT_INTELLIGENCE,affectableStats.getStat(CharStats.STAT_INTELLIGENCE)+1);
		affectableStats.setStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ,affectableStats.getStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ)+1);
		affectableStats.setStat(CharStats.STAT_WISDOM,affectableStats.getStat(CharStats.STAT_WISDOM)-1);
		affectableStats.setStat(CharStats.STAT_MAX_WISDOM_ADJ,affectableStats.getStat(CharStats.STAT_MAX_WISDOM_ADJ)-1);
		affectableStats.setStat(CharStats.STAT_SAVE_MIND,affectableStats.getStat(CharStats.STAT_SAVE_MIND)+10);
		affectableStats.setStat(CharStats.STAT_SAVE_OVERLOOKING,affectableStats.getStat(CharStats.STAT_SAVE_OVERLOOKING)+10);
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
			s1.setName(L("a small patchy tunic"));
			s1.setDisplayText(L("a small patchy tunic has been left here."));
			s1.setDescription(L("This small tunic is made of bits and pieces of many other shirts, it seems.  There are lots of tiny hidden compartments on it, and loops for hanging tools."));
			s1.text();
			outfitChoices.add(s1);

			final Armor s2=CMClass.getArmor("GenShoes");
			s2.setName(L("a pair of small shoes"));
			s2.setDisplayText(L("a pair of small shoes lie here."));
			s2.setDescription(L("This pair of small shoes appears to be a hodgepodge of materials and workmanship."));
			s2.text();
			outfitChoices.add(s2);

			final Armor p1=CMClass.getArmor("GenPants");
			p1.setName(L("a pair of small patchy pants"));
			p1.setDisplayText(L("a pair of small patchy pants lie here."));
			p1.setDescription(L("This pair of small pants is made of bits and pieces of many other pants, it seems.  There are lots of tiny hidden compartments on it, and loops for hanging tools."));
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
			return L("^r@x1^r is curiously close to death.^N",mob.name(viewer));
		else
		if(pct<.20)
			return L("^r@x1^r is covered in excessive bloody wounds.^N",mob.name(viewer));
		else
		if(pct<.30)
			return L("^r@x1^r is bleeding badly from a plethora of small wounds.^N",mob.name(viewer));
		else
		if(pct<.40)
			return L("^y@x1^y has numerous bloody wounds and unexpected gashes.^N",mob.name(viewer));
		else
		if(pct<.50)
			return L("^y@x1^y has some alarming wounds and small gashes.^N",mob.name(viewer));
		else
		if(pct<.60)
			return L("^p@x1^p has some small unwanted bloody wounds.^N",mob.name(viewer));
		else
		if(pct<.70)
			return L("^p@x1^p is cut and bruised in strange places.^N",mob.name(viewer));
		else
		if(pct<.80)
			return L("^g@x1^g has some small cuts and bruises.^N",mob.name(viewer));
		else
		if(pct<.90)
			return L("^g@x1^g has a few bruises and interesting scratches.^N",mob.name(viewer));
		else
		if(pct<.99)
			return L("^g@x1^g has a few small curious bruises.^N",mob.name(viewer));
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
				(L("a pair of @x1 eyes",name().toLowerCase()),RawMaterial.RESOURCE_MEAT));
				resources.addElement(makeResource
				(L("some @x1 blood",name().toLowerCase()),RawMaterial.RESOURCE_BLOOD));
				resources.addElement(makeResource
				(L("a pile of @x1 bones",name().toLowerCase()),RawMaterial.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
