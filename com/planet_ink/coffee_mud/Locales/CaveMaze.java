package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.Sense;
import java.util.*;

public class CaveMaze extends StdMaze
{
	public CaveMaze()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_DARK);
		recoverEnvStats();
		domainType=Room.DOMAIN_INDOORS_CAVE;

		domainCondition=Room.CONDITION_NORMAL;
	}
	public Environmental newInstance()
	{
		return new CaveMaze();
	}
	public int getMaxRange()
	{
		if(maxRange>=0) return maxRange;
		return 5;
	}
	public String getChildLocaleID(){return "CaveRoom";}
}
