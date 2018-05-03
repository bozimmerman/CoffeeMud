package com.planet_ink.coffee_mud.Items.CompTech;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.GenericBuilder;
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
public class GenWindGenerator extends GenFuellessGenerator
{
	@Override
	public String ID()
	{
		return "GenWindGenerator";
	}

	private volatile double lastWindFactor = 1.0;
	
	public GenWindGenerator()
	{
		super();
		setName("a wind power generator");
		setDisplayText("a wind power generator sits here.");
		setDescription("");
	}

	@Override
	protected int getAdjustedGeneratedAmountPerTick()
	{
		return (int)Math.round(generatedAmtPerTick * lastWindFactor);
	}

	@Override
	protected boolean canGenerateRightNow()
	{
		if(activated())
		{
			final Room R=CMLib.map().roomLocation(this);
			if((R!=null)
			&&((R.domainType()&Room.INDOORS)==0)
			&&(!CMLib.flags().isUnderWateryRoom(R))
			&&((R.getAtmosphere() & RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_GAS))
			{
				final Area A=R.getArea();
				final Climate C = (A!=null) ? A.getClimateObj() : null;
				lastWindFactor = 1.0;
				if(C!=null)
				{
					switch(C.weatherType(R))
					{
					case Climate.WEATHER_WINDY:
						lastWindFactor *= 2.0;
						break;
					case Climate.WEATHER_THUNDERSTORM:
					case Climate.WEATHER_BLIZZARD:
					case Climate.WEATHER_DUSTSTORM:
						lastWindFactor *= 1.5;
						break;
					case Climate.WEATHER_WINTER_COLD:
						lastWindFactor *= .75;
						break;
					case Climate.WEATHER_RAIN:
					case Climate.WEATHER_SNOW:
					case Climate.WEATHER_HAIL:
					case Climate.WEATHER_SLEET:
						break;
					case Climate.WEATHER_CLEAR:
					case Climate.WEATHER_CLOUDY:
					case Climate.WEATHER_HEAT_WAVE:
					case Climate.WEATHER_DROUGHT:
						lastWindFactor *= .5;
						break;
					}
				}
				if((R.getClimateType()&Room.CLIMASK_WINDY)!=0)
					lastWindFactor *= 1.75;
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenWindGenerator))
			return false;
		return super.sameAs(E);
	}
}
