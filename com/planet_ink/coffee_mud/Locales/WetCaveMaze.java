package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.Sense;
import java.util.*;

public class WetCaveMaze extends StdMaze
{
	public String ID(){return "WetCaveMaze";}
	public WetCaveMaze()
	{
		super();
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_DARK);
		recoverEnvStats();
		domainType=Room.DOMAIN_INDOORS_CAVE;

		domainCondition=Room.CONDITION_WET;
	}

	public String getChildLocaleID(){return "WetCaveRoom";}
	public int getMaxRange()
	{
		if(maxRange>=0) return maxRange;
		return 5;
	}
	public Vector resourceChoices(){return CaveRoom.roomResources;}
}
