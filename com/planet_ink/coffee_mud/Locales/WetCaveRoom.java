package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class WetCaveRoom extends CaveRoom
{
	public String ID(){return "WetCaveRoom";}
	public WetCaveRoom()
	{
		super();
		domainCondition=Room.CONDITION_WET;
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new WetCaveRoom();
	}
	public int getMaxRange()
	{
		if(maxRange>=0) return maxRange;
		return 5;
	}
	public Vector resourceChoices(){return CaveRoom.roomResources;}
}
