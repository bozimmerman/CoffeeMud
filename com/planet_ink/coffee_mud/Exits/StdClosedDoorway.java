package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdClosedDoorway extends StdExit
{
	public String ID(){	return "StdClosedDoorway";}
	public StdClosedDoorway()
	{
		super();
		name="a Door";
		description="An ordinary wooden door with swinging hinges and a latch.";
		displayText="an open door";
		closedText="a closed door";
		miscText="KEY";
		hasADoor=true;
		isOpen=false;
		hasALock=false;
		isLocked=false;
		doorDefaultsClosed=true;
		doorDefaultsLocked=false;
		openDelayTicks=45;
	}
	public Environmental newInstance()
	{
		return new StdClosedDoorway();
	}

}
