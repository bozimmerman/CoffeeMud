package com.planet_ink.coffee_mud.Items.MiscTech;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdShipItem extends StdElecItem implements Electronics
{
	public String ID(){	return "StdShipItem";}
	public StdShipItem()
	{
		super();
		setName("a piece of ship electronics");
		setDisplayText("a small piece of ship electronics sits here.");
		setDescription("You can't tell what it is by looking at it.");

		material=EnvResource.RESOURCE_STEEL;
		baseGoldValue=0;
		recoverEnvStats();
	}
}
