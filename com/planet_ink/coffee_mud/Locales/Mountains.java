package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class Mountains extends StdRoom
{
	public String ID(){return "Mountains";}
	public Mountains()
	{
		super();
		name="the mountain";
		baseEnvStats.setWeight(5);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_MOUNTAINS;
		domainCondition=Room.CONDITION_NORMAL;
	}

	public static final Integer[] resourceList={
		new Integer(EnvResource.RESOURCE_STONE),
		new Integer(EnvResource.RESOURCE_IRON),
		new Integer(EnvResource.RESOURCE_LEAD),
		new Integer(EnvResource.RESOURCE_SILVER),
		new Integer(EnvResource.RESOURCE_COPPER),
		new Integer(EnvResource.RESOURCE_TIN),
		new Integer(EnvResource.RESOURCE_AMETHYST),
		new Integer(EnvResource.RESOURCE_GARNET),
		new Integer(EnvResource.RESOURCE_AMBER),
		new Integer(EnvResource.RESOURCE_HERBS),
		new Integer(EnvResource.RESOURCE_OPAL),
		new Integer(EnvResource.RESOURCE_TOPAZ),
		new Integer(EnvResource.RESOURCE_BASALT),
		new Integer(EnvResource.RESOURCE_SHALE),
		new Integer(EnvResource.RESOURCE_PUMICE),
		new Integer(EnvResource.RESOURCE_SANDSTONE),
		new Integer(EnvResource.RESOURCE_SOAPSTONE),
		new Integer(EnvResource.RESOURCE_AQUAMARINE),
		new Integer(EnvResource.RESOURCE_CRYSOBERYL),
		new Integer(EnvResource.RESOURCE_ONYX),
		new Integer(EnvResource.RESOURCE_TURQUIOSE),
		new Integer(EnvResource.RESOURCE_DIAMOND),
		new Integer(EnvResource.RESOURCE_CRYSTAL),
		new Integer(EnvResource.RESOURCE_QUARTZ),
		new Integer(EnvResource.RESOURCE_PLATINUM)};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public Vector resourceChoices(){return Mountains.roomResources;}
}
