package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenDoor extends GenExit
{
	public String ID(){	return "GenDoor";}
	public String Name(){ return "a door";}
	public String displayText(){ return "";}
	public String closedText(){return "a closed door";}
	public GenDoor()
	{
		super();
		name="a door";
		displayText="";
		description="An ordinary wooden door with swinging hinges and a latch.";
		hasADoor=true;
		hasALock=false;
		doorDefaultsClosed=true;
		doorDefaultsLocked=false;
		closedText="a closed door";
		doorName="door";
		closeName="close";
		openName="open";
	}
	public Environmental newInstance()
	{
		return new GenDoor();
	}
}
