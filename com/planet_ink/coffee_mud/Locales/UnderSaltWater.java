package com.planet_ink.coffee_mud.Locales;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.*;
public class UnderSaltWater extends UnderWater
{
	public String ID(){return "UnderSaltWater";}
	public UnderSaltWater()
	{
		super();
	}

	public Environmental newInstance()
	{
		return new UnderSaltWater();
	}
	public int liquidType(){return EnvResource.RESOURCE_SALTWATER;}
	public static final Integer[] resourceList={
		new Integer(EnvResource.RESOURCE_SEAWEED),
		new Integer(EnvResource.RESOURCE_FISH),
		new Integer(EnvResource.RESOURCE_TUNA),
		new Integer(EnvResource.RESOURCE_SHRIMP),
		new Integer(EnvResource.RESOURCE_SAND),
		new Integer(EnvResource.RESOURCE_CLAY),
		new Integer(EnvResource.RESOURCE_PEARL),
		new Integer(EnvResource.RESOURCE_LIMESTONE)};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public Vector resourceChoices(){return UnderWater.roomResources;}
}
