package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class Jungle extends StdRoom
{
	public Jungle()
	{
		super();
		name="the jungle";
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		baseEnvStats.setWeight(3);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_JUNGLE;
		domainCondition=Room.CONDITION_HOT;
	}
	public Environmental newInstance()
	{
		return new Jungle();
	}
	public static final Integer[] resourceList={
		new Integer(EnvResource.RESOURCE_JADE),
		new Integer(EnvResource.RESOURCE_SCALES),
		new Integer(EnvResource.RESOURCE_HEMP),
		new Integer(EnvResource.RESOURCE_SILK),
		new Integer(EnvResource.RESOURCE_FRUIT),
		new Integer(EnvResource.RESOURCE_APPLES),
		new Integer(EnvResource.RESOURCE_BERRIES),
		new Integer(EnvResource.RESOURCE_ORANGES),
		new Integer(EnvResource.RESOURCE_LEMONS),
		new Integer(EnvResource.RESOURCE_FUR),
		new Integer(EnvResource.RESOURCE_FEATHERS)
	};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public Vector resourceChoices(){return Jungle.roomResources;}
}
