package com.planet_ink.coffee_mud.Items;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenReadable extends GenItem
{
	public GenReadable()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a generic readable thing";
		displayText="a generic readable thing sits here.";
		description="Looks like something";
		setMaterial(EnvResource.RESOURCE_WOOD);
		isReadable=true;
		baseEnvStats().setWeight(1);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new GenReadable();
	}
	public boolean isGeneric(){return true;}
}
