package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdOpenDoorway extends StdExit
{
	public String ID(){	return "StdOpenDoorway";}
	public String Name(){ return "a walkway";}
	public String displayText(){ return "";}
	public String description(){ return "";}
	public Environmental newInstance()
	{
		return new StdOpenDoorway();
	}
}
