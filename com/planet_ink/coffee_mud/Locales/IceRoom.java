package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class IceRoom extends StdRoom
{
	public IceRoom()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
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
