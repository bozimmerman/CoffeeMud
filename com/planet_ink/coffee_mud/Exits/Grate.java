package com.planet_ink.coffee_mud.Exits;

import com.planet_ink.coffee_mud.interfaces.*;


public class Grate extends StdClosedDoorway
{
	public Grate()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
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
