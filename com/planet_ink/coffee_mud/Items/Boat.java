package com.planet_ink.coffee_mud.Items;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Boat extends StdRideable
{
	public String ID(){	return "Boat";}
	public Boat()
	{
		super();
		setName("a boat");
		setDisplayText("a boat is docked here.");
		setDescription("Looks like a boat");
		rideBasis=Rideable.RIDEABLE_WATER;
		material=EnvResource.RESOURCE_OAK;
	}
	public Environmental newInstance()
	{
		return new Boat();
	}
}
