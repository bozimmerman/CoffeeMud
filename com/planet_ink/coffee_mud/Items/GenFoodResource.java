package com.planet_ink.coffee_mud.Items;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenFoodResource extends GenFood implements EnvResource, Food
{
	public GenFoodResource()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="an edible resource";
		displayText="a pile of edible resource sits here.";
		description="";
		isReadable=false;
		setMaterial(EnvResource.RESOURCE_BERRIES);
		setNourishment(200);
		baseEnvStats().setWeight(0);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new GenFoodResource();
	}
}
