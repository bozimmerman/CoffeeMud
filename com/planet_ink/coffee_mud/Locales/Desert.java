package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class Desert extends StdRoom
{
	public Desert()
	{
		super();
		name="the desert";
		baseEnvStats.setWeight(2);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_DESERT;
		domainCondition=Room.CONDITION_HOT;
		baseThirst=4;
	}
	public Environmental newInstance()
	{
		return new Desert();
	}
	public static final Integer[] resourceList={
		new Integer(EnvResource.RESOURCE_CACTUS),
		new Integer(EnvResource.RESOURCE_SAND),
		new Integer(EnvResource.RESOURCE_LAMPOIL),
		new Integer(EnvResource.RESOURCE_PEPPERS),
		new Integer(EnvResource.RESOURCE_SCALES),
		new Integer(EnvResource.RESOURCE_DATES)
	};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public Vector resourceChoices(){return Desert.roomResources;}
}
