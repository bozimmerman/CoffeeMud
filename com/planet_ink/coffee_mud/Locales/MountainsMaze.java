package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class MountainsMaze extends StdMaze
{
	public String ID(){return "MountainsMaze";}
	public MountainsMaze()
	{
		super();
		baseEnvStats.setWeight(5);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_MOUNTAINS;
		domainCondition=Room.CONDITION_NORMAL;
	}
	public Environmental newInstance()
	{
		return new MountainsMaze();
	}
	public String getChildLocaleID(){return "Mountains";}
	public Vector resourceChoices(){return Mountains.roomResources;}
}
