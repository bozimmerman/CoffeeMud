package com.planet_ink.coffee_mud.Items;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Boat extends StdRideable
{
	public Boat()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a boat";
		displayText="a boat is docked here.";
		description="Looks like a boat";
		rideBasis=Rideable.RIDEABLE_WATER;
	}
	public Environmental newInstance()
	{
		return new Boat();
	}
}
