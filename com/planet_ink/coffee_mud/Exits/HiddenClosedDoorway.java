package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class HiddenClosedDoorway extends StdExit
{
	public String ID(){	return "HiddenClosedDoorway";}
	public HiddenClosedDoorway()
	{
		super();
		name="a Door";
		description="a cleverly concealed door.";
		displayText="an open door";
		closedText="a closed door";
		miscText="KEY";
		hasADoor=true;
		isOpen=false;
		hasALock=false;
		isLocked=false;
		doorDefaultsClosed=true;
		doorDefaultsLocked=false;
		openDelayTicks=45;
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_HIDDEN);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new HiddenClosedDoorway();
	}

}
