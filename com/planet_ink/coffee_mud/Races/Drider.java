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
   Copyright 2022-2023 Bo Zimmerman

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
public class Drider extends StdRace
{
	@Override
	public String ID()
	{
		return "Drider";
	}

	public Drider()
	{
		super();
		super.naturalAbilImmunities.add("Disease_Gonorrhea");
	}

	private final static String localizedStaticName = CMLib.lang().L("Arachnid");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 76;
	}

	@Override
	public int shortestFemale()
	{
		return 76;
	}

	@Override
	public int heightVariance()
	{
		return 5;
	}

	@Override
	public int lightestWeight()
	{
		return 250;
	}

	@Override
	public int weightVariance()
	{
		return 20;
	}

	@Override
	public long forbiddenWornBits()
	{
		return Wearable.WORN_LEGS|Wearable.WORN_FEET;
	}

	private final static String localizedStaticRacialCat = CMLib.lang().L("Arachnid");

	@Override
	public String racialCategory()
	{
		return localizedStaticRacialCat;
	}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={2 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,8 ,8 ,1 ,0 ,1 ,1 ,0 ,0 };

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

	private final String[]	racialEffectNames			= { "Spell_LightSensitivity" };
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


	private final String[]	culturalAbilityNames			= { "Drowish", "Spell_DarknessGlobe"};
	private final int[]		culturalAbilityProficiencies	= { 100, 100};
	private final int[]		culturalAbilityLevels			= { 0, 1};
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

	private static Vector<RawMaterial>	resources	= new Vector<RawMaterial>();

	@Override
	public int availabilityCode()
	{
		return Area.THEME_ALLTHEMES | Area.THEME_SKILLONLYMASK;
	}

	@Override
	public boolean fertile()
	{
		return false;
	}

	@Override
	public boolean useRideClass()
	{
		return true;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.disposition()|PhyStats.CAN_SEE_DARK|PhyStats.CAN_SEE_INFRARED);
	}

	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STAT_SAVE_POISON,affectableStats.getStat(CharStats.STAT_SAVE_POISON)+20);
	}

	@Override
	public void unaffectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.unaffectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STAT_SAVE_POISON,affectableStats.getStat(CharStats.STAT_SAVE_POISON)-20);
	}

	@Override
	public String arriveStr()
	{
		return "creeps in";
	}

	@Override
	public String leaveStr()
	{
		return "skitters";
	}

	@Override
	public Weapon myNaturalWeapon()
	{
		return super.funHumanoidWeapon();
	}

	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				for(int x=0;x<8;x++)
				{
					resources.addElement(makeResource
					(L("an @x1 leg",name().toLowerCase()),RawMaterial.RESOURCE_OAK));
				}
				for(int x=0;x<5;x++)
				{
					resources.addElement(makeResource
					(L("some @x1 guts",name().toLowerCase()),RawMaterial.RESOURCE_MEAT));
				}
			}
		}
		return resources;
	}
}
