package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class MountainsMaze extends StdMaze
{
	public MountainsMaze()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_MOUNTAINS;
		domainCondition=Room.CONDITION_NORMAL;
		baseMove=5;
	}
	public Environmental newInstance()
	{
		return new MountainsMaze();
	}
	public String getChildLocaleID(){return "Mountains";}
}
