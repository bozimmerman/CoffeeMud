package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class Shore extends StdRoom
{
	public String ID(){return "Shore";}
	public Shore()
	{
		super();
		name="the shore";
		baseEnvStats.setWeight(2);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_DESERT;
		domainCondition=Room.CONDITION_HOT;
		baseThirst=1;
	}
	public Environmental newInstance()
	{
		return new Shore();
	}
	public static final Integer[] resourceList={
		new Integer(EnvResource.RESOURCE_FISH),
		new Integer(EnvResource.RESOURCE_SAND)
	};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public Vector resourceChoices(){return Shore.roomResources;}
}
