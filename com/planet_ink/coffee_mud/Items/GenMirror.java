package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenMirror extends GenItem
{
	public String ID(){	return "GenMirror";}
	public GenMirror()
	{
		super();
		name="a generic mirror";
		baseEnvStats.setWeight(2);
		displayText="a generic mirror sits here.";
		description="You see yourself in it!";
		baseGoldValue=5;
		baseEnvStats().setLevel(1);
		recoverEnvStats();
		setMaterial(EnvResource.RESOURCE_GLASS);
	}
	public String description()
	{
		return "You see yourself in it!";
	}
	
	public Environmental newInstance()
	{
		return new GenMirror();
	}
}
