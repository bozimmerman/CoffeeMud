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
   Copyright 2002-2018 Bo Zimmerman

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
public class Bull extends Buffalo
{
	@Override
	public String ID()
	{
		return "Bull";
	}

	private final static String localizedStaticName = CMLib.lang().L("Bull");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 48;
	}

	@Override
	public int shortestFemale()
	{
		return 48;
	}

	@Override
	public int heightVariance()
	{
		return 6;
	}

	@Override
	public int lightestWeight()
	{
		return 350;
	}

	@Override
	public int weightVariance()
	{
		return 100;
	}

	private static Vector<RawMaterial>	resources	= new Vector<RawMaterial>();

	private final static String localizedStaticRacialCat = CMLib.lang().L("Bovine");

	@Override
	public String racialCategory()
	{
		return localizedStaticRacialCat;
	}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,0 ,0 ,1 ,4 ,4 ,1 ,0 ,1 ,1 ,1 ,0 };

	@Override
	public int[] bodyMask()
	{
		return parts;
	}

	@Override
	public boolean canBreedWith(Race R)
	{
		if((!super.sameAs(R))&&(R!=null))
			return R.ID().equals("Cow");
		return true;
	}

	@Override
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName(L("a pair of deadly horns"));
			naturalWeapon.setMaterial(RawMaterial.RESOURCE_BONE);
			naturalWeapon.setUsesRemaining(1000);
			naturalWeapon.setWeaponDamageType(Weapon.TYPE_PIERCING);
		}
		return naturalWeapon;
	}

	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setRacialStat(CharStats.STAT_STRENGTH,19);
		affectableStats.setRacialStat(CharStats.STAT_DEXTERITY,5);
		affectableStats.setRacialStat(CharStats.STAT_INTELLIGENCE,1);
	}

	@Override
	public Race mixRace(Race race, String newRaceID, String newRaceName)
	{
		if(ID().equalsIgnoreCase("Bull"))
		{
			if((race!=null)&&(race.ID().equalsIgnoreCase("Buffalo")))
				return CMClass.getRace("Buffalo");
			if((race!=null)&&(race.ID().equalsIgnoreCase("Cow")))
				return CMClass.getRace("Cow");
		}
		return super.mixRace(race, newRaceID, newRaceName);
	}

	@Override
	public String makeMobName(char gender, int age)
	{
		switch(age)
		{
			case Race.AGE_INFANT:
			case Race.AGE_TODDLER:
			case Race.AGE_CHILD:
				return "calf";
			case Race.AGE_YOUNGADULT:
			{
				switch(gender)
				{
				case 'M':
				case 'm':
					return "young bull";
				case 'F':
				case 'f':
					return "young cow";
				default:
					return name().toLowerCase();
				}
			}
			case Race.AGE_MATURE:
			case Race.AGE_MIDDLEAGED:
			default:
			{
				switch(gender)
				{
				case 'M':
				case 'm':
					return "bull";
				case 'F':
				case 'f':
					return "cow";
				default:
					return name().toLowerCase();
				}
			}
			case Race.AGE_OLD:
			case Race.AGE_VENERABLE:
			case Race.AGE_ANCIENT:
			{
				switch(gender)
				{
				case 'M':
				case 'm':
					return "old bull";
				case 'F':
				case 'f':
					return "old cow";
				default:
					return "old " + name().toLowerCase();
				}
			}
		}
	}

	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				(L("a pair of @x1 horns",name().toLowerCase()),RawMaterial.RESOURCE_BONE));
				for(int i=0;i<10;i++)
				{
					resources.addElement(makeResource
					(L("a strip of @x1 leather",name().toLowerCase()),RawMaterial.RESOURCE_LEATHER));
				}
				for(int i=0;i<7;i++)
				{
					resources.addElement(makeResource
					(L("a pound of @x1 meat",name().toLowerCase()),RawMaterial.RESOURCE_BEEF));
				}
				resources.addElement(makeResource
				(L("some @x1 blood",name().toLowerCase()),RawMaterial.RESOURCE_BLOOD));
				resources.addElement(makeResource
				(L("a pile of @x1 bones",name().toLowerCase()),RawMaterial.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
