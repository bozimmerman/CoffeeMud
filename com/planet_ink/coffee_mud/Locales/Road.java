package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class Road extends StdRoom
{
	public String ID(){return "Road";}
	public Road()
	{
		super();
		name="a road";
		baseEnvStats.setWeight(1);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_PLAINS;
		domainCondition=Room.CONDITION_NORMAL;
	}

	public static final Integer[] resourceList={
		new Integer(EnvResource.RESOURCE_STONE),
		new Integer(EnvResource.RESOURCE_FEATHERS),
		new Integer(EnvResource.RESOURCE_SCALES),
		new Integer(EnvResource.RESOURCE_SAND),
		new Integer(EnvResource.RESOURCE_CLAY),
	};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public Vector resourceChoices(){return Road.roomResources;}
}