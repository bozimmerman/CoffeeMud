package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_Weather extends Property
{
	public String ID() { return "Prop_Weather"; }
	public String name(){ return "Weather Setter";}
	protected int canAffectCode(){return Ability.CAN_AREAS;}

	int code=-1;
	
	public void affectEnvStats(Environmental host, EnvStats stats)
	{
		super.affectEnvStats(host,stats);
		if((code<0)&&(text().length()>0))
		{
			for(int i=0;i<Climate.WEATHER_DESCS.length;i++)
				if(Climate.WEATHER_DESCS[i].equalsIgnoreCase(text()))
					code=i;
		}
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
	}
	
}
