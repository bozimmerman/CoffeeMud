package com.planet_ink.coffee_mud.Items;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenReadable extends GenItem
{
	public String ID(){	return "GenReadable";}
	public GenReadable()
	{
		super();
		setName("a generic readable thing");
		setDisplayText("a generic readable thing sits here.");
		setDescription("");
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
