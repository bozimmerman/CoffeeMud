package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ClimbableExit extends StdExit
{
	public ClimbableExit()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a sheer surface";
		description="Looks like you'll have to climb it.";
		displayText="a sheer surface";
		miscText="";
		hasADoor=false;
		isOpen=true;
		hasALock=false;
		isLocked=false;
		doorDefaultsClosed=false;
		doorDefaultsLocked=false;
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_CLIMBING);
		recoverEnvStats();
		openDelayTicks=1;
	}
	public Environmental newInstance()
	{
		return new ClimbableExit();
	}
}
