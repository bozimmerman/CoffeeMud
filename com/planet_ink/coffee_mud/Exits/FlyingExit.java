package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class FlyingExit extends StdExit
{
	public String ID(){	return "FlyingExit";}
	public FlyingExit()
	{
		super();
		name="the open air";
		description="Looks like you'll have to fly up there.";
		displayText="";
		miscText="";
		hasADoor=false;
		isOpen=true;
		hasALock=false;
		isLocked=false;
		doorDefaultsClosed=false;
		doorDefaultsLocked=false;
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_FLYING);
		recoverEnvStats();
		openDelayTicks=1;
	}
	public Environmental newInstance()
	{
		return new FlyingExit();
	}
}
