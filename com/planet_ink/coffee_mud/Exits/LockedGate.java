package com.planet_ink.coffee_mud.Exits;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;


public class LockedGate extends Gate
{
	public String ID(){	return "LockedGate";}
	public String closedText(){return "a closed, locked gate";}
	public boolean hasALock(){return true;}
	public boolean defaultsLocked(){return true;}
	public Environmental newInstance()
	{
		return new LockedGate();
	}

}
