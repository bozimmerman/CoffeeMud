package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class MountainsGrid extends StdGrid
{
	public String ID(){return "MountainsGrid";}
	public MountainsGrid()
	{
		super();
		name="the mountains";
		baseEnvStats.setWeight(5);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_MOUNTAINS;
		domainCondition=Room.CONDITION_NORMAL;
	}

	public String getChildLocaleID(){return "Mountains";}
	public Vector resourceChoices(){return Mountains.roomResources;}
}
