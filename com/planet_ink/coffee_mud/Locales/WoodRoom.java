package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class WoodRoom extends StdRoom
{
	public String ID(){return "WoodRoom";}
	public WoodRoom()
	{
		super();
		baseEnvStats.setWeight(1);
		recoverEnvStats();
		domainType=Room.DOMAIN_INDOORS_WOOD;
		domainCondition=Room.CONDITION_NORMAL;
	}
	public Environmental newInstance()
	{
		return new WoodRoom();
	} 
}
