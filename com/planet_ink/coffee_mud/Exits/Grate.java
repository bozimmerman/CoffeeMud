package com.planet_ink.coffee_mud.Exits;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;


public class Grate extends StdClosedDoorway
{
	public String ID(){	return "Grate";}
	public Grate()
	{
		super();
		name="a barred grate";
		description="A metal grate of thick steel bars is inset here.";
		displayText="a dark hole";
		closedText="a closed grate";
		doorName="grate";
		openName="remove";
		closeName="close";
	}
	public Environmental newInstance()
	{
		return new Grate();
	}

}
