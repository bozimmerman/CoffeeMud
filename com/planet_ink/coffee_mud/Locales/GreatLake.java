package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class GreatLake extends StdGrid
{
	public GreatLake()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
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
