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
   Copyright 2001-2025 Bo Zimmerman

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
public class Human extends StdRace
{
	@Override
	public String ID()
	{
		return "Human";
	}

	private final static String localizedStaticName = CMLib.lang().L("Human");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 68;
	}

	@Override
	public int shortestFemale()
	{
		return 64;
	}

	@Override
	public int heightVariance()
	{
		return 12;
	}

	@Override
	public int lightestWeight()
	{
		return 150;
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
	protected int trainsAtFirstLevel()
	{
		return 2;
	}

	private final static String localizedStaticRacialCat = CMLib.lang().L("Humanoid");

	@Override
	public String racialCategory()
	{
		return localizedStaticRacialCat;
	}

	private final String[]	culturalAbilityNames			= { "Chopping" };
	private final int[]		culturalAbilityProficiencies	= { 50 };

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

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,0 ,0 };

	@Override
	public int[] bodyMask()
	{
		return parts;
	}

	private final int[]	agingChart	= { 0, 1, 3, 15, 35, 53, 70, 74, 78 };

	@Override
	public int[] getAgingChart()
	{
		return agingChart;
	}

	private static Vector<RawMaterial>	resources	= new Vector<RawMaterial>();

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY | Area.THEME_TECHNOLOGY;
	}

	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STAT_SAVE_JUSTICE,affectableStats.getStat(CharStats.STAT_SAVE_JUSTICE)+10);
	}

	@Override
	public void unaffectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.unaffectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STAT_SAVE_JUSTICE,affectableStats.getStat(CharStats.STAT_SAVE_JUSTICE)-10);
	}

	private final String[]	racialEffectNames			= { "DiligentStudying" };
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
			final Armor s2=CMClass.getArmor("GenShoes");
			outfitChoices.add(s2);
			final Armor p1=CMClass.getArmor("GenPants");
			outfitChoices.add(p1);
			final Armor s3=CMClass.getArmor("GenBelt");
			outfitChoices.add(s3);
			cleanOutfit(outfitChoices);
		}
		return outfitChoices;
	}

	@Override
	public Weapon[] getNaturalWeapons()
	{
		if(naturalWeaponChoices.length==0)
			naturalWeaponChoices = getHumanoidWeapons();
		return super.getNaturalWeapons();
	}

	@Override
	public String healthText(final MOB viewer, final MOB mob)
	{
		final double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return L("^r@x1^r is mortally wounded and will soon die.^N",mob.name(viewer));
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
			return L("^p@x1^p is cut and bruised.^N",mob.name(viewer));
		else
		if(pct<.80)
			return L("^g@x1^g has some minor cuts and bruises.^N",mob.name(viewer));
		else
		if(pct<.90)
			return L("^g@x1^g has a few bruises and scratches.^N",mob.name(viewer));
		else
		if(pct<.99)
			return L("^g@x1^g has a few small bruises.^N",mob.name(viewer));
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
				(L("a @x1 liver",name().toLowerCase()),RawMaterial.RESOURCE_MEAT));
				resources.addElement(makeResource
				(L("some @x1 blood",name().toLowerCase()),RawMaterial.RESOURCE_BLOOD));
				resources.addElement(makeResource
				(L("a pile of @x1 bones",name().toLowerCase()),RawMaterial.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
