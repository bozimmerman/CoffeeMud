package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class StoneRoom extends StdRoom
{
	public String ID(){return "StoneRoom";}
	public StoneRoom()
	{
		super();
		recoverEnvStats();
		domainType=Room.DOMAIN_INDOORS_STONE;
		domainCondition=Room.CONDITION_NORMAL;
		baseEnvStats.setWeight(4);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new StoneRoom();
	}
}
