package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.Sense;
import java.util.*;

public class CaveRoom extends StdRoom
{
	public CaveRoom()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="the cave";
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_DARK);
		recoverEnvStats();
		domainType=Room.DOMAIN_INDOORS_CAVE;

		domainCondition=Room.CONDITION_NORMAL;
	}
	public Environmental newInstance()
	{
		return new CaveRoom();
	}
	public int getMaxRange()
	{
		if(maxRange>=0) return maxRange;
		return 5;
	}
	public static final Integer[] resourceList={
		new Integer(EnvResource.RESOURCE_GRANITE),
		new Integer(EnvResource.RESOURCE_OBSIDIAN),
		new Integer(EnvResource.RESOURCE_MARBLE),
		new Integer(EnvResource.RESOURCE_STONE),
		new Integer(EnvResource.RESOURCE_IRON),
		new Integer(EnvResource.RESOURCE_LEAD),
		new Integer(EnvResource.RESOURCE_BRONZE),
		new Integer(EnvResource.RESOURCE_GOLD),
		new Integer(EnvResource.RESOURCE_SILVER),
		new Integer(EnvResource.RESOURCE_ZINC),
		new Integer(EnvResource.RESOURCE_COPPER),
		new Integer(EnvResource.RESOURCE_TIN),
		new Integer(EnvResource.RESOURCE_MITHRIL),
		new Integer(EnvResource.RESOURCE_ADAMANTITE),
		new Integer(EnvResource.RESOURCE_GEM),
		new Integer(EnvResource.RESOURCE_SCALES),
		new Integer(EnvResource.RESOURCE_CRYSTAL),
		new Integer(EnvResource.RESOURCE_PLATINUM)};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public Vector resourceChoices(){return CaveRoom.roomResources;}
}
