package com.planet_ink.coffee_mud.Locales;
import com.planet_ink.coffee_mud.interfaces.*;
public class SaltWaterSurface extends WaterSurface
{
	public SaltWaterSurface()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}

	public Environmental newInstance()
	{
		return new SaltWaterSurface();
	}
	public int liquidType(){return EnvResource.RESOURCE_SALTWATER;}
}
