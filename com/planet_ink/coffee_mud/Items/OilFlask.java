package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

public class OilFlask extends StdDrink
{

	public int amountOfLiquidHeld=5;
	public int amountOfLiquidRemaining=5;

	public OilFlask()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="an oil flask";
		baseEnvStats.setWeight(10);
		capacity=0;
		setMaterial(EnvResource.RESOURCE_GLASS);
		displayText="an oil flask sits here.";
		description="A small glass flask containing lamp oil, with a lid.";
		baseGoldValue=5;
		liquidType=EnvResource.RESOURCE_LAMPOIL;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new OilFlask();
	}
}