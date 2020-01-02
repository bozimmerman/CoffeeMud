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
   Copyright 2002-2020 Bo Zimmerman

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
public class Crab extends StdRace
{
	@Override
	public String ID()
	{
		return "Crab";
	}

	private final static String localizedStaticName = CMLib.lang().L("Crab");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 12;
	}

	@Override
	public int shortestFemale()
	{
		return 10;
	}

	@Override
	public int heightVariance()
	{
		return 2;
	}

	@Override
	public int lightestWeight()
	{
		return 10;
	}

	@Override
	public int weightVariance()
	{
		return 5;
	}

	@Override
	public long forbiddenWornBits()
	{
		return ~(Wearable.WORN_EYES|Wearable.WORN_ABOUT_BODY|Wearable.WORN_FLOATING_NEARBY|Wearable.WORN_MOUTH|Wearable.WORN_LEGS);
	}


	@Override
	public int[] getBreathables()
	{
		return breatheAirWaterArray;
	}

	private final String[]	racialAbilityNames			= { "Aquan", "Skill_Swim" };
	private final int[]		racialAbilityLevels			= { 1,1 };
	private final int[]		racialAbilityProficiencies	= { 100,100 };
	private final boolean[]	racialAbilityQuals			= { false,false };
	private final String[]	racialAbilityParms			= { "", "" };

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

	private final String[]	racialEffectNames			= { "Aquan"};
	private final int[]		racialEffectLevels			= { 1};
	private final String[]	racialEffectParms			= { "SPOKEN=TRUE" };

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

	private final static String localizedStaticRacialCat = CMLib.lang().L("Fish");

	@Override
	public String racialCategory()
	{
		return localizedStaticRacialCat;
	}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={2 ,2, 2 ,1 ,0 ,2 ,0 ,1 ,4 ,0 ,0 ,2 ,1 ,0 ,0 ,0 };

	@Override
	public int[] bodyMask()
	{
		return parts;
	}

	private final int[]	agingChart	= { 0, 1, 2, 5, 10, 15, 15, 20, 20 };

	@Override
	public int[] getAgingChart()
	{
		return agingChart;
	}

	private static Vector<RawMaterial>	resources	= new Vector<RawMaterial>();

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY | Area.THEME_SKILLONLYMASK;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
	}

	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setRacialStat(CharStats.STAT_STRENGTH,3);
		affectableStats.setRacialStat(CharStats.STAT_DEXTERITY,3);
		affectableStats.setRacialStat(CharStats.STAT_INTELLIGENCE,1);
	}

	@Override
	public String arriveStr()
	{
		return "scuttles in";
	}

	@Override
	public String leaveStr()
	{
		return "scuttles";
	}

	@Override
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName(L("a nasty claw"));
			naturalWeapon.setMaterial(RawMaterial.RESOURCE_BONE);
			naturalWeapon.setUsesRemaining(1000);
			naturalWeapon.setWeaponDamageType(Weapon.TYPE_BASHING);
		}
		return naturalWeapon;
	}

	@Override
	public String makeMobName(final char gender, final int age)
	{
		switch(age)
		{
			case Race.AGE_INFANT:
			case Race.AGE_TODDLER:
			case Race.AGE_CHILD:
				return "baby "+name().toLowerCase();
			default :
				return super.makeMobName('N', age);
		}
	}

	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				for(int i=0;i<6;i++)
				{
					resources.addElement(makeResource
					(L("some @x1 legs",name().toLowerCase()),RawMaterial.RESOURCE_MEAT));
				}
				resources.addElement(makeResource
				(L("a @x1 claw",name().toLowerCase()),RawMaterial.RESOURCE_MEAT));
				resources.addElement(makeResource
				(L("a @x1 claw",name().toLowerCase()),RawMaterial.RESOURCE_MEAT));
				resources.addElement(makeResource
				(L("a @x1 shell",name().toLowerCase()),RawMaterial.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
