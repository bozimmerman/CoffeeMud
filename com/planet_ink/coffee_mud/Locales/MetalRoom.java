package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class MetalRoom extends StdRoom
{
	public String ID(){return "MetalRoom";}
	public MetalRoom()
	{
		super();
		name="the room";
		baseEnvStats.setWeight(1);
		recoverEnvStats();
		domainType=Room.DOMAIN_INDOORS_METAL;
		domainCondition=Room.CONDITION_NORMAL;
	}

}
