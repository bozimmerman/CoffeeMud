package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenAirLock extends GenExit
{
	public String ID(){	return "GenAirLock";}
	public String Name(){ return "an air lock";}
	public String displayText(){ return "";}
	public boolean hasADoor(){return true;}
	public boolean hasALock(){return false;}
	public boolean defaultsLocked(){return false;}
	public boolean defaultsClosed(){return true;}
	public String closedText(){return "a closed air lock door";}
	public GenAirLock()
	{
		super();
		name="an air lock door";
		displayText="";
		description="This door leads to the outside of the ship through a small air lock.";
		hasADoor=true;
		hasALock=false;
		doorDefaultsClosed=true;
		doorDefaultsLocked=false;
		closedText="a closed air lock door";
		doorName="door";
		closeName="close";
		openName="open";
	}
	public Environmental newInstance()
	{
		return new GenAirLock();
	}
}
