package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class WoodRoomMaze extends StdMaze
{
	public String ID(){return "WoodRoomMaze";}
	public WoodRoomMaze()
	{
		super();
		baseEnvStats.setWeight(1);
		recoverEnvStats();
		domainType=Room.DOMAIN_INDOORS_WOOD;
		domainCondition=Room.CONDITION_NORMAL;
	}

	public String getChildLocaleID(){return "WoodRoom";}
}
