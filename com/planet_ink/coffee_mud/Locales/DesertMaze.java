package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class DesertMaze extends StdMaze
{
	public String ID(){return "DesertMaze";}
	public DesertMaze()
	{
		super();
		baseEnvStats.setWeight(2);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_DESERT;
		domainCondition=Room.CONDITION_HOT;
		baseThirst=4;
	}

	public String getChildLocaleID(){return "Desert";}
	public Vector resourceChoices(){return Desert.roomResources;}
}
