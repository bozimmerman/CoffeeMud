package com.planet_ink.coffee_mud.Behaviors;
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
public class LinkedWeather extends StdBehavior
{
	@Override
	public String ID()
	{
		return "LinkedWeather";
	}

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_AREAS;
	}

	protected long lastWeather=-1;
	protected long lastPending=-1;
	protected String areaName=null;
	protected boolean rolling=false;

	@Override
	public String accountForYourself()
	{
		return "weather event linking";
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(tickID!=Tickable.TICKID_AREA)
			return true;
		if(!(ticking instanceof Area))
			return true;
		if(areaName==null)
		{
			if(getParms().length()==0)
				return true;
			String s=getParms();
			final int x=s.indexOf(';');
			rolling=false;
			if(x>=0)
			{
				if(s.indexOf("ROLL",x+1)>=0)
					rolling=true;
				s=s.substring(0,x);
			}
			final Area A=CMLib.map().getArea(s);
			if(A!=null)
				areaName=A.Name();
		}

		final Area A=(Area)ticking;
		final Area linkedA=CMLib.map().getArea(areaName);
		if(linkedA!=null)
		{
			if(rolling)
				A.getClimateObj().setNextWeatherType(linkedA.getClimateObj().weatherType(null));
			else
			{
				A.getClimateObj().setCurrentWeatherType(linkedA.getClimateObj().weatherType(null));
				A.getClimateObj().setNextWeatherType(linkedA.getClimateObj().nextWeatherType(null));
			}
		}
		return true;
	}
}
