package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class SwampGrid extends StdGrid
{
	public SwampGrid()
	{
		super();
		name="the swamp";
		baseEnvStats.setWeight(3);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_SWAMP;
		domainCondition=Room.CONDITION_WET;
	}
	public Environmental newInstance()
	{
		return new SwampGrid();
	}
	public String getChildLocaleID(){return "Swamp";}
	public Vector resourceChoices(){return Swamp.roomResources;}
}
