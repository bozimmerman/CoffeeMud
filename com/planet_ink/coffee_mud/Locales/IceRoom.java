package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class IceRoom extends StdRoom
{
	public String ID(){return "IceRoom";}
	public IceRoom()
	{
		super();
		baseEnvStats.setWeight(1);
		recoverEnvStats();
		domainType=Room.DOMAIN_INDOORS_STONE;
		domainCondition=Room.CONDITION_COLD;
	}
	public Environmental newInstance()
	{
		return new IceRoom();
	}
}
