package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class StoneMaze extends StdMaze
{
	public String ID(){return "StoneMaze";}
	public StoneMaze()
	{
		super();
		domainType=Room.DOMAIN_INDOORS_STONE;
		domainCondition=Room.CONDITION_NORMAL;
		baseEnvStats.setWeight(1);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new StoneMaze();
	}
	public String getChildLocaleID(){return "StoneRoom";}
}
