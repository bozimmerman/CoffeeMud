package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdLockedDoorway extends StdClosedDoorway
{
	public String ID(){	return "StdLockedDoorway";}
	public boolean hasALock(){return true;}
	public boolean defaultsLocked(){return true;}
	public String closedText(){return "a closed, locked door";}
}
