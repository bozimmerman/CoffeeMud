package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class StoneGrid extends StdRoom
{
	public StoneGrid()
	{
		super();
		domainType=Room.DOMAIN_INDOORS_STONE;
		domainCondition=Room.CONDITION_NORMAL;
		baseEnvStats.setWeight(1);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new StoneGrid();
	}
	public String getChildLocaleID(){return "StoneRoom";}
}
