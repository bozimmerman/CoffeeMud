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
   Copyright 2025-2025 Bo Zimmerman

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
public class Beastkin extends StdRace
{
	@Override
	public String ID()
	{
		return "Beastkin";
	}

	private final static String localizedStaticName = CMLib.lang().L("Beastkin");

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

	private final static String localizedStaticRacialCat = CMLib.lang().L("Animal");

	@Override
	public String racialCategory()
	{
		return localizedStaticRacialCat;
	}
	private final String[]	culturalAbilityNames			= { "Hunting", "Butchering", "Druid_ShapeShift", "Fey" };
	private final int[]		culturalAbilityProficiencies	= { 70, 50, 50, 50 };
	private final boolean[]	culturalAbilityGains			= { true, true, true, true };

	@Override
	protected boolean[] culturalAbilityAutoGains()
	{
		return culturalAbilityGains;
	}

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
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,0 ,0 ,1 ,4 ,4 ,1 ,0 ,1 ,1 ,1 ,0 };

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
		return Area.THEME_ALLTHEMES | Area.THEME_SKILLONLYMASK;
	}


	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_INFRARED);
	}

	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.adjStat(CharStats.STAT_CONSTITUTION,2);
		affectableStats.adjStat(CharStats.STAT_DEXTERITY,1);
		affectableStats.adjStat(CharStats.STAT_CHARISMA,-1);
		affectableStats.adjStat(CharStats.STAT_INTELLIGENCE,-2);
		affectableStats.setStat(CharStats.STAT_SAVE_COLD,affectableStats.getStat(CharStats.STAT_SAVE_COLD)+20);
		affectableStats.setStat(CharStats.STAT_SAVE_FIRE,affectableStats.getStat(CharStats.STAT_SAVE_FIRE)-20);
	}

	@Override
	public void unaffectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.unaffectCharStats(affectedMOB, affectableStats);
		affectableStats.adjStat(CharStats.STAT_CONSTITUTION,-2);
		affectableStats.adjStat(CharStats.STAT_DEXTERITY,-1);
		affectableStats.adjStat(CharStats.STAT_CHARISMA,1);
		affectableStats.adjStat(CharStats.STAT_INTELLIGENCE,2);
		affectableStats.setStat(CharStats.STAT_SAVE_COLD,affectableStats.getStat(CharStats.STAT_SAVE_COLD)-20);
		affectableStats.setStat(CharStats.STAT_SAVE_FIRE,affectableStats.getStat(CharStats.STAT_SAVE_FIRE)+20);
	}

	@Override
	public Weapon[] getNaturalWeapons()
	{
		return super.getHumanoidWeapons();
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
			s1.setRawProperLocationBitmap(Wearable.WORN_LEFT_WRIST|Wearable.WORN_RIGHT_WRIST);
			s1.setRawLogicalAnd(false);
			s1.setName(L("^#333a fur wristguard^?"));
			s1.setDisplayText(L("a fur wristguard sits here."));
			s1.setDescription(L("You may not have the time of day wearing this on your wrist, but you do have the ^/fashion^? of the day."));
			s1.text();
			outfitChoices.add(s1);

			final Armor p1=CMClass.getArmor("GenPants");
			p1.setName(L("^#420a^? ^#430small^? ^#420hide^?^#430 loincloth^?"));
			p1.setDisplayText(L("a primitive loincloth has been left here."));
			p1.setDescription(L("This is some hide that will cover your junk."));
			p1.text();
			outfitChoices.add(p1);
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
				(L("some @x1 claws",name().toLowerCase()),RawMaterial.RESOURCE_BONE));
				resources.addElement(makeResource
				(L("a strip of @x1 fur",name().toLowerCase()),RawMaterial.RESOURCE_FUR));
				resources.addElement(makeResource
				(L("a pound of @x1 meat",name().toLowerCase()),RawMaterial.RESOURCE_MEAT));
				resources.addElement(makeResource
				(L("some @x1 blood",name().toLowerCase()),RawMaterial.RESOURCE_BLOOD));
				resources.addElement(makeResource
				(L("a pile of @x1 bones",name().toLowerCase()),RawMaterial.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
