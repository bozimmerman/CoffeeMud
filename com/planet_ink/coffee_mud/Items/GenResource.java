package com.planet_ink.coffee_mud.Items;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenResource extends GenItem implements EnvResource
{
	public GenResource()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a pile of resource thing";
		displayText="a pile of resource sits here.";
		description="Looks like resource";
		isReadable=false;
		setMaterial(EnvResource.RESOURCE_IRON);
		baseEnvStats().setWeight(0);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new GenResource();
	}
}
