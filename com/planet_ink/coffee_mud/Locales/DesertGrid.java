package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class DesertGrid extends StdGrid
{
	public DesertGrid()
	{
		super();
		name="the desert";
		baseEnvStats.setWeight(2);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_DESERT;
		domainCondition=Room.CONDITION_HOT;
	}
	public Environmental newInstance()
	{
		return new DesertGrid();
	}
	public String getChildLocaleID(){return "Desert";}
	public Vector resourceChoices(){return Desert.roomResources;}
}
