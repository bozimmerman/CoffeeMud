package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class Plains extends StdRoom
{
	public Plains()
	{
		super();
		name="the grass";
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		baseEnvStats.setWeight(2);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_PLAINS;
		domainCondition=Room.CONDITION_NORMAL;
	}
	public Environmental newInstance()
	{
		return new Plains();
	}
	public static final Integer[] resourceList={
		new Integer(EnvResource.RESOURCE_WHEAT),
		new Integer(EnvResource.RESOURCE_CORN),
		new Integer(EnvResource.RESOURCE_RICE),
		new Integer(EnvResource.RESOURCE_CARROTS),
		new Integer(EnvResource.RESOURCE_TOMATOES),
		new Integer(EnvResource.RESOURCE_FLINT),
		new Integer(EnvResource.RESOURCE_COTTON),
		new Integer(EnvResource.RESOURCE_MEAT),
		new Integer(EnvResource.RESOURCE_EGGS),
		new Integer(EnvResource.RESOURCE_BEEF),
		new Integer(EnvResource.RESOURCE_HIDE),
		new Integer(EnvResource.RESOURCE_FUR),
		new Integer(EnvResource.RESOURCE_FEATHERS),
		new Integer(EnvResource.RESOURCE_LEATHER),
		new Integer(EnvResource.RESOURCE_WOOL)};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public Vector resourceChoices(){return Plains.roomResources;}
}
