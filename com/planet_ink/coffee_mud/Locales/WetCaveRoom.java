package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class WetCaveRoom extends CaveRoom
{
	public WetCaveRoom()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
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
