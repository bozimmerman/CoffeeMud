package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class WoodsGrid extends StdGrid
{
	public String ID(){return "WoodsGrid";}
	public WoodsGrid()
	{
		super();
		baseEnvStats.setWeight(3);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_WOODS;
		domainCondition=Room.CONDITION_NORMAL;
	}
	public Environmental newInstance()
	{
		return new WoodsGrid();
	}
	public String getChildLocaleID(){return "Woods";}
	public Vector resourceChoices(){return Woods.roomResources;}
}
