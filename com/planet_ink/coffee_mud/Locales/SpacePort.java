package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class SpacePort extends StdRoom
{
	public String ID(){return "SpacePort";}
	public SpacePort()
	{
		super();
		name="the space port";
		baseEnvStats.setWeight(1);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_SPACEPORT;
		domainCondition=Room.CONDITION_NORMAL;
	}

}
