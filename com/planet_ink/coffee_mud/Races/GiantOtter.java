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

import java.util.List;
import java.util.Vector;
/*
   Copyright 2023-2025 Bo Zimmerman

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
public class GiantOtter extends Otter
{
	@Override
	public String ID()
	{
		return "GiantOtter";
	}

	private final static String localizedStaticName = CMLib.lang().L("Giant Otter");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 59;
	}

	@Override
	public int shortestFemale()
	{
		return 39;
	}

	@Override
	public int heightVariance()
	{
		return 12;
	}

	@Override
	public int lightestWeight()
	{
		return 50;
	}

	@Override
	public int weightVariance()
	{
		return 20;
	}

	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		affectableStats.setRacialStat(CharStats.STAT_INTELLIGENCE,1);
		affectableStats.setRacialStat(CharStats.STAT_STRENGTH,14);
		affectableStats.setRacialStat(CharStats.STAT_DEXTERITY,12);
	}

	private static Vector<RawMaterial>	resources	= new Vector<RawMaterial>();

	@Override
	protected List<RawMaterial> privateResources() { return resources; }

	@Override
	public List<RawMaterial> myResources()
	{
		final List<RawMaterial>	resources	= privateResources();
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				for(int i=0;i<5;i++)
					resources.add(makeResource
					(L("a @x1 fur",name().toLowerCase()),RawMaterial.RESOURCE_FUR,L("a strip of @x1 fur",name().toLowerCase())));
				resources.add(makeResource
				(L("some mustelid blood",name().toLowerCase()),RawMaterial.RESOURCE_BLOOD));
				for(int i=0;i<2;i++)
					resources.add(makeResource
					(L("a pile of @x1 bones",name().toLowerCase()),RawMaterial.RESOURCE_BONE));
				resources.add(makeResource
				(L("some @x1 claws",name().toLowerCase()),RawMaterial.RESOURCE_BONE));
				resources.add(makeResource
				(L("a @x1 tail",name().toLowerCase()),RawMaterial.RESOURCE_MEAT));
				for(int i=0;i<4;i++)
					resources.add(makeResource
					(L("a pound of @x1 meat",name().toLowerCase()),RawMaterial.RESOURCE_MEAT));
			}
		}
		return resources;
	}
}
