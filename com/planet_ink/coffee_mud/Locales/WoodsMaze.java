package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class WoodsMaze extends StdMaze
{
	public WoodsMaze()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_WOODS;
		domainCondition=Room.CONDITION_NORMAL;
		baseMove=3;
	}
	public Environmental newInstance()
	{
		return new WoodsMaze();
	}
	public String getChildLocaleID(){return "Woods";}
}
