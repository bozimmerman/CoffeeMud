package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class Woods extends StdRoom
{
	public Woods()
	{
		super();
		name="the woods";
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		baseEnvStats.setWeight(3);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_WOODS;
		domainCondition=Room.CONDITION_NORMAL;
	}
	public Environmental newInstance()
	{
		return new Woods();
	}
	public static final Integer[] resourceList={
		new Integer(EnvResource.RESOURCE_WOOD),
		new Integer(EnvResource.RESOURCE_PINE),
		new Integer(EnvResource.RESOURCE_BALSA),
		new Integer(EnvResource.RESOURCE_OAK),
		new Integer(EnvResource.RESOURCE_MAPLE),
		new Integer(EnvResource.RESOURCE_REDWOOD),
		new Integer(EnvResource.RESOURCE_HICKORY),
		new Integer(EnvResource.RESOURCE_FRUIT),
		new Integer(EnvResource.RESOURCE_APPLES),
		new Integer(EnvResource.RESOURCE_BERRIES),
		new Integer(EnvResource.RESOURCE_ORANGES),
		new Integer(EnvResource.RESOURCE_LEMONS),
		new Integer(EnvResource.RESOURCE_FUR),
		new Integer(EnvResource.RESOURCE_HIDE),
		new Integer(EnvResource.RESOURCE_FEATHERS),
		new Integer(EnvResource.RESOURCE_LEATHER)};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public Vector resourceChoices(){return Woods.roomResources;}
}
