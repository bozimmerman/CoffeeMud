package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdClosedDoorway extends StdExit
{
	public String ID(){	return "StdClosedDoorway";}
	public String Name(){ return "a door";}
	public String displayText(){ return "";}
	public String description(){ return "An ordinary wooden door with swinging hinges and a latch.";}
	public boolean hasADoor(){return true;}
	public boolean hasALock(){return false;}
	public boolean defaultsLocked(){return false;}
	public boolean defaultsClosed(){return true;}
	public String closedText(){return "a closed door";}
}
