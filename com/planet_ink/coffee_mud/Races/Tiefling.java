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
   Copyright 2022-2025 Bo Zimmerman

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
public class Tiefling extends Humanoid
{
	@Override
	public String ID()
	{
		return "Tiefling";
	}

	public Tiefling()
	{
		super();
	}

	private final static String localizedStaticName = CMLib.lang().L("Tiefling");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 65;
	}

	@Override
	public int shortestFemale()
	{
		return 65;
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
		return 80;
	}

	private final static String localizedStaticRacialCat = CMLib.lang().L("Demon");

	@Override
	public String racialCategory()
	{
		return localizedStaticRacialCat;
	}

	@Override
	public long forbiddenWornBits()
	{
		return Wearable.WORN_ABOUT_BODY | Wearable.WORN_HEAD | Wearable.WORN_BACK;
	}

	private final int[]	agingChart	= { 0, 1, 5, 21, 55, 77, 108, 116, 124 };

	@Override
	public int[] getAgingChart()
	{
		return agingChart;
	}

	private final String[]	racialAbilityNames			= { "WingFlying"};
	private final int[]		racialAbilityLevels			= { 30 };
	private final int[]		racialAbilityProficiencies	= { 100 };
	private final boolean[]	racialAbilityQuals			= { true };
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

	private final String[]	racialEffectNames			= { "Prayer_TaintOfChaos" };
	private final int[]		racialEffectLevels			= { 1, };
	private final String[]	racialEffectParms			= {  "" };

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

	private final String[]	culturalAbilityNames			= { "Undercommon", "Spell_DarknessGlobe"};
	private final int[]		culturalAbilityProficiencies	= { 50, 0};
	private final int[]		culturalAbilityLevels			= { 0, 1 };
	private final boolean[] culturalAbilityAutoGains		= { true, true};

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

	@Override
	public int[] culturalAbilityLevels()
	{
		return culturalAbilityLevels;
	}

	@Override
	protected boolean[] culturalAbilityAutoGains()
	{
		return culturalAbilityAutoGains;
	}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,1 ,2 };

	@Override
	public int[] bodyMask()
	{
		return parts;
	}

	protected static Vector<RawMaterial> resources=new Vector<RawMaterial>();

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_DARK);
	}

	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		affectableStats.adjStat(CharStats.STAT_INTELLIGENCE,1);
		affectableStats.adjStat(CharStats.STAT_CHARISMA,2);
		affectableStats.setStat(CharStats.STAT_SAVE_FIRE,affectableStats.getStat(CharStats.STAT_SAVE_FIRE)+20);
		affectableStats.setStat(CharStats.STAT_SAVE_COLD,affectableStats.getStat(CharStats.STAT_SAVE_COLD)-10);
	}

	@Override
	public void unaffectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		affectableStats.adjStat(CharStats.STAT_INTELLIGENCE,-1);
		affectableStats.adjStat(CharStats.STAT_CHARISMA,-2);
		affectableStats.setStat(CharStats.STAT_SAVE_FIRE,affectableStats.getStat(CharStats.STAT_SAVE_FIRE)-20);
		affectableStats.setStat(CharStats.STAT_SAVE_COLD,affectableStats.getStat(CharStats.STAT_SAVE_COLD)+10);
	}

	@Override
	public int getXPAdjustment()
	{
		return -20;
	}

	@Override
	public String getStatAdjDesc()
	{
		return super.getStatAdjDesc();
	}

	@Override
	public List<Item> outfit(final MOB myChar)
	{
		super.outfit(myChar);
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
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				(L("a @x1 horn",name().toLowerCase()),RawMaterial.RESOURCE_BONE));
				resources.addElement(makeResource
				(L("a @x1 wing",name().toLowerCase()),RawMaterial.RESOURCE_DRAGONSCALES));
				resources.addElement(makeResource
				(L("some humanoid blood",name().toLowerCase()),RawMaterial.RESOURCE_BLOOD));
				resources.addElement(makeResource
				(L("a humanoid brain",name().toLowerCase()),RawMaterial.RESOURCE_MEAT));
			}
		}
		return resources;
	}
}
