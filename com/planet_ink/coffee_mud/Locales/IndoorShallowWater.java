package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class IndoorShallowWater extends ShallowWater implements Drink
{
	public IndoorShallowWater()
	{
		super();
		name="the water";
		recoverEnvStats();
		domainType=Room.DOMAIN_INDOORS_CAVE;
		domainCondition=Room.CONDITION_WET;
		baseThirst=0;
	}
	public Environmental newInstance()
	{
		return new IndoorShallowWater();
	}
	public Vector resourceChoices(){return CaveRoom.roomResources;}
}
