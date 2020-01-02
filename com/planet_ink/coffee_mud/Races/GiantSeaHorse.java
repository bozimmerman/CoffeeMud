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
   Copyright 2018-2020 Bo Zimmerman

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
public class GiantSeaHorse extends SeaHorse
{
	@Override
	public String ID()
	{
		return "GiantSeaHorse";
	}

	private final static String localizedStaticName = CMLib.lang().L("Giant Seahorse");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 86;
	}

	@Override
	public int shortestFemale()
	{
		return 84;
	}

	@Override
	public int heightVariance()
	{
		return 16;
	}

	@Override
	public int lightestWeight()
	{
		return 300;
	}

	@Override
	public int weightVariance()
	{
		return 30;
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY | Area.THEME_SKILLONLYMASK;
	}

	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		affectableStats.setRacialStat(CharStats.STAT_INTELLIGENCE,1);
		affectableStats.adjStat(CharStats.STAT_DEXTERITY,4);
		affectableStats.adjStat(CharStats.STAT_MAX_DEXTERITY_ADJ,4);
		affectableStats.adjStat(CharStats.STAT_DEXTERITY,2);
		affectableStats.adjStat(CharStats.STAT_MAX_DEXTERITY_ADJ,2);
		affectableStats.adjStat(CharStats.STAT_CONSTITUTION,2);
		affectableStats.adjStat(CharStats.STAT_MAX_CONSTITUTION_ADJ,2);
	}

	@Override
	public boolean useRideClass()
	{
		return true;
	}

	private static Vector<RawMaterial>	resources	= new Vector<RawMaterial>();

	private final int[]	agingChart	= { 0, 1, 2, 5, 10, 15, 15, 20, 20 };

	@Override
	public int[] getAgingChart()
	{
		return agingChart;
	}

	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				for(int i=0;i<5;i++)
				{
					resources.addElement(makeResource
					(L("some @x1",name().toLowerCase()),RawMaterial.RESOURCE_FISH));
				}
				for(int i=0;i<10;i++)
				{
					resources.addElement(makeResource
					(L("a @x1 bone",name().toLowerCase()),RawMaterial.RESOURCE_BONE,L("@x1 bones",name().toLowerCase())));
				}
				for(int i=0;i<3;i++)
				{
					resources.addElement(makeResource
					(L("some @x1 blood",name().toLowerCase()),RawMaterial.RESOURCE_BLOOD));
				}
			}
		}
		return resources;
	}
}
