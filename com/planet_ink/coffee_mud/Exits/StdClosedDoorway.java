package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdClosedDoorway extends StdExit
{
	public StdClosedDoorway()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
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
