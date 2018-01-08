package com.planet_ink.coffee_mud.Abilities.Properties;
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
   Copyright 2004-2018 Bo Zimmerman

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
public class Prop_Weather extends Property
{
	@Override
	public String ID()
	{
		return "Prop_Weather";
	}

	@Override
	public String name()
	{
		return "Weather Setter";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_AREAS;
	}

	int code=-1;
	int climask=-1;

	@Override
	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);
		List<String> parms=CMParms.parse(newMiscText);
		code=-1;
		climask=-1;
		if(text().length()>0)
		{
			for(String parm : parms)
			{
				if(parm.startsWith("CLIMASK_")||parm.startsWith("CLIMATE_"))
				{
					parm=parm.substring(8);
					for(int i=0;i<Places.CLIMATE_DESCS.length;i++)
					{
						if(Places.CLIMATE_DESCS[i].equalsIgnoreCase(parm))
						{
							if(code<0)
								code=0;
							if(i>0)
								code=code|((int)CMath.pow(2,i-1));
						}
					}
				}
				else
				{
					for(int i=0;i<Climate.WEATHER_DESCS.length;i++)
					{
						if(Climate.WEATHER_DESCS[i].equalsIgnoreCase(parm))
							code=i;
					}
				}
			}
		}
	}
	
	@Override
	public void affectPhyStats(Physical host, PhyStats stats)
	{
		super.affectPhyStats(host,stats);
		if(code>=0)
		{
			if(affected instanceof Room)
			{
				((Room)affected).getArea().getClimateObj().setCurrentWeatherType(code);
				((Room)affected).getArea().getClimateObj().setNextWeatherType(code);
			}
			else
			if(affected instanceof Area)
			{
				((Area)affected).getClimateObj().setCurrentWeatherType(code);
				((Area)affected).getClimateObj().setNextWeatherType(code);
			}
		}
		if(climask>=0)
		{
			if(affected instanceof Room)
				((Room)affected).getArea().setClimateType(climask);
			else
				((Area)affected).setClimateType(climask);
		}
	}

}
