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
   Copyright 2016-2018 Bo Zimmerman

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
public class Seal extends GiantFish
{
	@Override
	public String ID()
	{
		return "Seal";
	}

	private final static String localizedStaticName = CMLib.lang().L("Seal");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 50;
	}

	@Override
	public int shortestFemale()
	{
		return 55;
	}

	@Override
	public int heightVariance()
	{
		return 10;
	}

	@Override
	public int lightestWeight()
	{
		return 155;
	}

	@Override
	public int weightVariance()
	{
		return 15;
	}

	@Override
	public int[] getBreathables()
	{
		return breatheAirWaterArray;
	}
	
	@Override
	public long forbiddenWornBits()
	{
		return ~(Wearable.WORN_ABOUT_BODY|Wearable.WORN_BACK|Wearable.WORN_EYES|
				Wearable.WORN_HEAD|Wearable.WORN_MOUTH|Wearable.WORN_WAIST);
	}

	private final static String localizedStaticRacialCat = CMLib.lang().L("Sea Mammal");

	@Override
	public String racialCategory()
	{
		return localizedStaticRacialCat;
	}

	private static Vector<RawMaterial>		resources					= new Vector<RawMaterial>();
	private final String[]					racialAbilityNames			= { "Aquan","Skill_Swim" };
	private final int[]						racialAbilityLevels			= { 1,1 };
	private final int[]						racialAbilityProficiencies	= { 100,100 };
	private final boolean[]					racialAbilityQuals			= { false,false };
	private final String[]					racialAbilityParms			= { "", "" };

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
	private static final int[] parts={0 ,2 ,0 ,1 ,1 ,0 ,0 ,1 ,0 ,0 ,1 ,2 ,1 ,1 ,1 ,0 };

	@Override
	public int[] bodyMask()
	{
		return parts;
	}

	private final int[]	agingChart	= { 0, 1, 2, 3, 4, 5, 6, 7, 8 };

	@Override
	public int[] getAgingChart()
	{
		return agingChart;
	}

	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		//super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setRacialStat(CharStats.STAT_INTELLIGENCE,1);
		affectableStats.setRacialStat(CharStats.STAT_DEXTERITY,15);
	}

	@Override
	public String arriveStr()
	{
		return "flops in";
	}

	@Override
	public String leaveStr()
	{
		return "flops";
	}

	@Override
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName(L("a heat butt"));
			naturalWeapon.setMaterial(RawMaterial.RESOURCE_BONE);
			naturalWeapon.setUsesRemaining(1000);
			naturalWeapon.setWeaponDamageType(Weapon.TYPE_BASHING);
		}
		return naturalWeapon;
	}

	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				for(int i=0;i<8;i++)
				{
					resources.addElement(makeResource
					(L("some @x1",name().toLowerCase()),RawMaterial.RESOURCE_FISH));
				}
				for(int i=0;i<5;i++)
				{
					resources.addElement(makeResource
					(L("a fuzzy @x1 hide",name().toLowerCase()),RawMaterial.RESOURCE_HIDE));
				}
				resources.addElement(makeResource
				(L("some @x1 blood",name().toLowerCase()),RawMaterial.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}
