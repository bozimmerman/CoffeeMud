package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class WoodRoomGrid extends StdGrid
{
	public String ID(){return "WoodRoomGrid";}
	public WoodRoomGrid()
	{
		super();
		baseEnvStats.setWeight(1);
		recoverEnvStats();
		domainType=Room.DOMAIN_INDOORS_WOOD;
		domainCondition=Room.CONDITION_NORMAL;
	}
	public Environmental newInstance()
	{
		return new WoodRoomGrid();
	}
	public String getChildLocaleID(){return "WoodRoom";}
}
