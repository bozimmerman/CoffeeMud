package com.planet_ink.coffee_mud.Exits;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;


public class Gate extends StdClosedDoorway
{
	public String ID(){	return "Gate";}
	public Gate()
	{
		super();
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
