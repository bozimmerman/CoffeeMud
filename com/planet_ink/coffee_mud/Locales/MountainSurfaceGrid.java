package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class MountainSurfaceGrid extends StdGrid
{
	public MountainSurfaceGrid()
	{
		super();
		name="the mountains";
		baseEnvStats.setWeight(5);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_MOUNTAINS;
		domainCondition=Room.CONDITION_NORMAL;
	}
	public Environmental newInstance()
	{
		return new MountainSurfaceGrid();
	}
	public String getChildLocaleID(){return "MountainSurface";}
	public Vector resourceChoices(){return Mountains.roomResources;}
}
