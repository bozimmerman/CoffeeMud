package com.planet_ink.coffee_mud.Exits;

import com.planet_ink.coffee_mud.interfaces.*;


public class Gate extends StdClosedDoorway
{
	public Gate()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a barred gate";
		description="A pair of study metal bar doors.";
		displayText="an open gate";
		closedText="a closed gate";
		doorName="gate";
		openName="open";
		closeName="close";
	}
	public Environmental newInstance()
	{
		return new Gate();
	}
	
}
