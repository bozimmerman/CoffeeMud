package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class HillsGrid extends StdGrid
{
	public String ID(){return "HillsGrid";}
	public HillsGrid()
	{
		super();
		name="the hills";
		baseEnvStats.setWeight(3);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_HILLS;
		domainCondition=Room.CONDITION_NORMAL;
	}
	public Environmental newInstance()
	{
		return new HillsGrid();
	}
	public String getChildLocaleID(){return "Hills";}
	public Vector resourceChoices(){return Hills.roomResources;}
}
