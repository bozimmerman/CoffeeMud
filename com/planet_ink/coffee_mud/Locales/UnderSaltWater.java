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
	public Vector resourceChoices(){return UnderWater.roomResources;}
	
}
