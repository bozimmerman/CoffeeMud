package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class Hills extends StdRoom
{
	public String ID(){return "Hills";}
	public Hills()
	{
		super();
		name="the hills";
		baseEnvStats.setWeight(3);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_HILLS;
		domainCondition=Room.CONDITION_NORMAL;
	}
	public Environmental newInstance()
	{
		return new Hills();
	}
	public static final Integer[] resourceList={
		new Integer(EnvResource.RESOURCE_GRAPES),
		new Integer(EnvResource.RESOURCE_BERRIES),
		new Integer(EnvResource.RESOURCE_OLIVES),
		new Integer(EnvResource.RESOURCE_RICE),
		new Integer(EnvResource.RESOURCE_LEATHER),
		new Integer(EnvResource.RESOURCE_FEATHERS),
		new Integer(EnvResource.RESOURCE_EGGS),
		new Integer(EnvResource.RESOURCE_HERBS),
		new Integer(EnvResource.RESOURCE_POTATOES)
	};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public Vector resourceChoices(){return Hills.roomResources;}
}
