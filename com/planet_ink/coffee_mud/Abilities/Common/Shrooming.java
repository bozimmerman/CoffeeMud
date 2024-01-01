package com.planet_ink.coffee_mud.Abilities.Common;
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
   Copyright 2022-2024 Bo Zimmerman

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
public class Shrooming extends Farming
{
	@Override
	public String ID()
	{
		return "Shrooming";
	}

	private final static String	localizedName	= CMLib.lang().L("Shrooming");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "SHROOMING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected int getDuration(final MOB mob, final int level)
	{
		return getDuration(113, mob, level, 37);
	}

	@Override
	protected int baseYield()
	{
		return 3;
	}
	
	@Override
	public boolean isPotentialCrop(final Room R, final int code)
	{
		if(R==null)
			return false;
		return true;
	}

	@Override
	protected boolean plantableResource(final int rsc)
	{
		if((rsc==RawMaterial.RESOURCE_MUSHROOMS)
		||(rsc==RawMaterial.RESOURCE_FUNGUS))
			return true;
		return false;
	}

	@Override
	protected boolean allowedInTheDark()
	{
		return true;
	}

	@Override
	protected String seedWord()
	{
		return "spore";
	}
	
	@Override
	protected boolean canGrowHere(final MOB mob, final Room R, final boolean quiet)
	{
		if(R==null)
			return false;
		final Area A=R.getArea();
		if(A==null)
			return false;
		if(A.getClimateObj().canSeeTheSun(R)
		&&(A.getTimeObj().getTODCode()==TimeClock.TimeOfDay.DAY))
		{
			if(!quiet)
				commonTelL(mob,"You to be in a dark place to do shrooming.  Check the time and weather.");
			return false;
		}
		if(A.getClimateObj().weatherType(R)==Climate.WEATHER_DROUGHT)
		{
			if(!quiet)
				commonTelL(mob,"The current drought conditions make shrooming useless.");
			return false;
		}
		return true;
	}

}
