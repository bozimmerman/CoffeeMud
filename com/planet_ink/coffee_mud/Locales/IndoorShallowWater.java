package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class IndoorShallowWater extends ShallowWater implements Drink
{
	public String ID(){return "IndoorShallowWater";}
	public IndoorShallowWater()
	{
		super();
		name="the water";
		recoverEnvStats();
		domainType=Room.DOMAIN_INDOORS_WATERSURFACE;
		domainCondition=Room.CONDITION_WET;
		baseThirst=0;
	}

	public Vector resourceChoices(){return CaveRoom.roomResources;}
}
