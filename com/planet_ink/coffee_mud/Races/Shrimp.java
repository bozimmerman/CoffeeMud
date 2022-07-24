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
   Copyright 2022-2022 Bo Zimmerman

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
public class Shrimp extends Fish
{
	@Override
	public String ID()
	{
		return "Shrimp";
	}

	private final static String localizedStaticName = CMLib.lang().L("Shrimp");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 1;
	}

	@Override
	public int shortestFemale()
	{
		return 1;
	}

	@Override
	public int heightVariance()
	{
		return 0;
	}

	@Override
	public int lightestWeight()
	{
		return 1;
	}

	@Override
	public int weightVariance()
	{
		return 0;
	}

	@Override
	public long forbiddenWornBits()
	{
		return ~(Wearable.WORN_EYES);
	}

	@Override
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName(L("its pointy parts"));
			naturalWeapon.setMaterial(RawMaterial.RESOURCE_SHRIMP);
			naturalWeapon.setUsesRemaining(1000);
			naturalWeapon.setWeaponDamageType(Weapon.TYPE_BASHING);
		}
		return naturalWeapon;
	}

	private static Vector<RawMaterial>	resources					= new Vector<RawMaterial>();
	
	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				(L("a @x1 tail",name().toLowerCase()),RawMaterial.RESOURCE_SHRIMP));
				resources.addElement(makeResource
				(L("a @x1 scale",name().toLowerCase()),RawMaterial.RESOURCE_SCALES,L("@x1 scale",name().toLowerCase())));
			}
		}
		return resources;
	}
}
