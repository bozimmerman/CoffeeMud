package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class MountainsGrid extends StdGrid
{
	public MountainsGrid()
	{
		super();
		name="the jungle";
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		baseEnvStats.setWeight(5);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_MOUNTAINS;
		domainCondition=Room.CONDITION_NORMAL;
	}
	public Environmental newInstance()
	{
		return new MountainsGrid();
	}
	public String getChildLocaleID(){return "MountainSurface";}
	public Vector resourceChoices(){return Mountains.roomResources;}
}
