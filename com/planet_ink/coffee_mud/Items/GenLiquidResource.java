package com.planet_ink.coffee_mud.Items;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenLiquidResource extends GenWater implements EnvResource, Drink
{
	public GenLiquidResource()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a puddle of resource thing";
		displayText="a puddle of resource sits here.";
		description="Looks like resource";
		isReadable=false;
		setMaterial(EnvResource.RESOURCE_FRESHWATER);
		disappearsAfterDrinking=true;
		baseEnvStats().setWeight(0);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new GenLiquidResource();
	}
}
