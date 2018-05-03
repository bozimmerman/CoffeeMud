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
   Copyright 2003-2018 Bo Zimmerman

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
public class GiantAmphibian extends GreatAmphibian
{
	@Override
	public String ID()
	{
		return "GiantAmphibian";
	}

	private final static String localizedStaticName = CMLib.lang().L("Giant Amphibian");

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
		return 20;
	}

	@Override
	public int lightestWeight()
	{
		return 1955;
	}

	@Override
	public int weightVariance()
	{
		return 405;
	}

	@Override
	public long forbiddenWornBits()
	{
		return ~(Wearable.WORN_EYES);
	}

	private final static String localizedStaticRacialCat = CMLib.lang().L("Amphibian");

	@Override
	public String racialCategory()
	{
		return localizedStaticRacialCat;
	}

	private static Vector<RawMaterial>	resources	= new Vector<RawMaterial>();

	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				for(int i=0;i<25;i++)
				resources.addElement(makeResource
				(L("some @x1",name().toLowerCase()),RawMaterial.RESOURCE_FISH));
				for(int i=0;i<15;i++)
				resources.addElement(makeResource
				(L("a @x1 hide",name().toLowerCase()),RawMaterial.RESOURCE_HIDE));
				resources.addElement(makeResource
				(L("some @x1 blood",name().toLowerCase()),RawMaterial.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}
