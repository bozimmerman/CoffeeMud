package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdOpenDoorway extends StdExit
{
	public String ID(){	return "StdOpenDoorway";}
	public StdOpenDoorway()
	{
		super();
		name="a walkway";
		description="";
		displayText="";
		miscText="";
		hasADoor=false;
		isOpen=true;
		hasALock=false;
		isLocked=false;
		doorDefaultsClosed=false;
		doorDefaultsLocked=false;
		openDelayTicks=1;
	}
	public Environmental newInstance()
	{
		return new StdOpenDoorway();
	}
}
