package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class GreatLake extends StdGrid
{
	public String ID(){return "GreatLake";}
	public GreatLake()
	{
		super();
		name="the lake";
		baseEnvStats.setWeight(2);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_WATERSURFACE;
		domainCondition=Room.CONDITION_WET;
	}
	public Environmental newInstance()
	{
		return new GreatLake();
	}
	public String getChildLocaleID(){return "WaterSurface";}
	public Vector resourceChoices(){return UnderWater.roomResources;}
}
