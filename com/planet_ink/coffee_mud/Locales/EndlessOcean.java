package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class EndlessOcean extends StdGrid
{
	public EndlessOcean()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="the ocean";
		baseEnvStats.setWeight(2);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_WATERSURFACE;
		domainCondition=Room.CONDITION_WET;
	}
	public Environmental newInstance()
	{
		return new EndlessOcean();
	}
	public String getChildLocaleID(){return "SaltWaterSurface";}
	public Vector resourceChoices(){return UnderWater.roomResources;}
}
